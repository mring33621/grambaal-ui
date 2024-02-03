package xyz.mattring.grambaal.ui.users;

import org.mindrot.jbcrypt.BCrypt;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class UsrMgt {
    static final int FOUR_MINUTES = 4 * 60 * 1000;

    static boolean isEmpty(String s) {
        return s == null || s.trim().isEmpty();
    }

    static boolean isExpired(long millistamp, long currentTimeMillis) {
        return (millistamp + FOUR_MINUTES) < currentTimeMillis;
    }

    static String generateLoginToken() {
        return UUID.randomUUID().toString();
    }

    static String readLastLineFromFile(String filePath) {
        try {
            List<String> lines = Files.readAllLines(Paths.get(filePath));
            if (!lines.isEmpty()) {
                return lines.getLast().trim();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    static enum CredsFormat {
        INVALID, PLAIN_TEXT, HASH_AND_SALT
    }

    static CredsFormat checkCredsFormat(String maybeCreds) {
        CredsFormat result = CredsFormat.INVALID;
        if (!isEmpty(maybeCreds)) {
            final String[] parts = maybeCreds.split("\\|");
            if (parts.length == 3) {
                result = CredsFormat.HASH_AND_SALT;
            } else if (parts.length == 2) {
                result = CredsFormat.PLAIN_TEXT;
            }
        }
        return result;
    }

    static boolean isValidCredsEntryPresentInFile(String file, String plaintextCreds) {
        if (isEmpty(plaintextCreds)) {
            return false;
        }

        final CredsFormat format = checkCredsFormat(plaintextCreds);
        if (format == CredsFormat.INVALID || format == CredsFormat.HASH_AND_SALT) {
            return false;
        }

        if (format == CredsFormat.PLAIN_TEXT) {
            final String[] tryUnamePwd = plaintextCreds.split("\\|");
            final String tryUname = tryUnamePwd[0];
            final String tryPwd = tryUnamePwd[1];
            try {
                // TODO: maybe move synchronized pwdFile block here
                final List<String> linesStartingWithUname =
                        Files.readAllLines(Paths.get(file)).stream()
                                .filter(l -> l.startsWith(tryUname + "|"))
                                .toList();
                for (String line : linesStartingWithUname) {
                    if (CredsFormat.HASH_AND_SALT == checkCredsFormat(line)) {
                        final String[] parts = line.split("\\|");
                        final String uname = parts[0];
                        final String hash = parts[1];
                        if (uname.equals(tryUname) && BCrypt.checkpw(tryPwd, hash)) { // double check uname along w/ pwd
                            return true;
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return false;
    }

    static boolean isEmptyOrContainsWhitespace(String s) {
        return isEmpty(s) || s.contains(" ");
    }

    private final String pwdFile; // I think we should synchronize on pwdFile when we read/write it
    private final Map<String, Long> logins;

    public UsrMgt(final String appName, final String optionalBaseDir) {

        if (isEmptyOrContainsWhitespace(appName)) {
            throw new IllegalArgumentException("appName must be non-empty and cannot contain whitespace");
        }

        final String baseDir = isEmpty(optionalBaseDir) ? System.getProperty("user.home") : optionalBaseDir;
        this.pwdFile = String.format("%s/%s_um.txt", baseDir, appName);
        try {
            Files.createDirectories(Paths.get(baseDir));
            if (!Files.exists(Paths.get(this.pwdFile))) {
                Files.createFile(Paths.get(this.pwdFile));
            }
        } catch (IOException ioex) {
            throw new UncheckedIOException("Could not create user management file", ioex);
        }

        this.logins = new java.util.concurrent.ConcurrentHashMap<>();

        final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
        executorService.scheduleAtFixedRate(
                this::replacePlainTextPwdsWithHashAndSalt,
                1, 117, TimeUnit.SECONDS);
        executorService.scheduleAtFixedRate(
                this::removeExpiredLogins,
                180, 60, TimeUnit.SECONDS);
        Runtime.getRuntime().addShutdownHook(new Thread(executorService::shutdown));
    }

    public boolean hasAtLeastOneUserProvisioned() {
        synchronized (this.pwdFile) {
            return CredsFormat.INVALID != checkCredsFormat(readLastLineFromFile(this.pwdFile));
        }
    }

    void removeExpiredLogins() {
        logins.entrySet().removeIf(e -> isExpired(e.getValue(), System.currentTimeMillis()));
    }

    void replacePlainTextPwdsWithHashAndSalt() {
        try {
            final java.util.List<String> lines;
            synchronized (this.pwdFile) {
                lines = new LinkedList<>(Files.readAllLines(Paths.get(this.pwdFile)));
            }
            final java.util.List<String> newLines = new java.util.ArrayList<>();
            boolean changed = false;
            for (String line : lines) {
                if (CredsFormat.PLAIN_TEXT == checkCredsFormat(line)) {
                    final String[] parts = line.split("\\|");
                    final String salt = BCrypt.gensalt();
                    final String hash = BCrypt.hashpw(parts[1], salt);
                    // TODO: don't need to store the salt, but it's here for now
                    newLines.add(String.format("%s|%s|%s", parts[0], hash, salt));
                    changed = true;
                } else {
                    newLines.add(line);
                }
            }
            if (changed) {
                synchronized (this.pwdFile) {
                    Files.write(Paths.get(this.pwdFile), newLines);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Optional<String> tryLogin(final String credsOrToken) {
        if (isEmpty(credsOrToken)) {
            return Optional.empty();
        }
        // check for token
        if (validateToken(credsOrToken, false)) {
            return Optional.of(credsOrToken);
        }
        // check password
        final boolean validCreds;
        synchronized (pwdFile) {
            validCreds = isValidCredsEntryPresentInFile(this.pwdFile, credsOrToken);
        }
        if (validCreds) {
            // Gratz! Valid creds get a token!
            final String token = generateLoginToken();
            logins.put(token, System.currentTimeMillis());
            return Optional.of(token);
        }
        // no good
        return Optional.empty();
    }

    public boolean validateToken(final String token, boolean renewIfValid) {
        if (isEmpty(token)) {
            return false;
        }
        final Long millistamp = logins.get(token);
        final boolean valid = millistamp != null && !isExpired(millistamp, System.currentTimeMillis());
        if (valid && renewIfValid) {
            logins.put(token, System.currentTimeMillis());
        }
        return valid;
    }

    public void logoutToken(final String token) {
        if (isEmpty(token)) {
            return;
        }
        logins.remove(token);
    }

}
