package xyz.mattring.grambaal.ui.users;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class UsrMgt {
    static final int FIVE_MINUTES = 5 * 60 * 1000;

    static boolean isEmpty(String s) {
        return s == null || s.trim().isEmpty();
    }

    static boolean isExpired(long millistamp, long currentTimeMillis) {
        return (millistamp + FIVE_MINUTES) < currentTimeMillis;
    }

    static String generateLoginToken() {
        return UUID.randomUUID().toString();
    }

    static String readLastLineFromFile(String filePath) {
        try {
            List<String> lines = Files.readAllLines(Paths.get(filePath));
            if (!lines.isEmpty()) {
                return lines.get(lines.size() - 1).trim();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    static boolean isEntryPresentInFile(String filePath, String entry) {
        try (java.io.BufferedReader br = new java.io.BufferedReader(new java.io.FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().equals(entry)) {
                    return true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    static boolean isValidCredsFormat(String s) {
        if (isEmpty(s)) {
            return false;
        }
        String[] parts = s.split("\\|");
        return parts.length == 2 && !isEmpty(parts[0]) && !isEmpty(parts[1]);
    }

    static boolean isValidCredsEntryPresentInFile(String file, String creds) {
        return isValidCredsFormat(creds) && isEntryPresentInFile(file, creds);
    }

    static boolean isEmptyOrContainsWhitespace(String s) {
        return isEmpty(s) || s.contains(" ");
    }

    private final String pwdFile;
    private final Map<String, Long> logins;

    public UsrMgt(final String appName) {
        if (isEmptyOrContainsWhitespace(appName)) {
            throw new IllegalArgumentException("appName must be non-empty and not contain whitespace");
        }
        this.pwdFile = String.format("%s/%s_um.txt", System.getProperty("user.home"), appName);
        this.logins = new java.util.concurrent.ConcurrentHashMap<>();
        final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
        executorService.scheduleAtFixedRate(
                this::removeExpiredLogins,
                1, 1, java.util.concurrent.TimeUnit.MINUTES);
        Runtime.getRuntime().addShutdownHook(new Thread(executorService::shutdown));
    }

    public boolean isProvisioned() {
        return isValidCredsFormat(readLastLineFromFile(this.pwdFile));
    }

    void removeExpiredLogins() {
        logins.entrySet().removeIf(e -> isExpired(e.getValue(), System.currentTimeMillis()));
    }

    public Optional<String> tryLogin(final String credsOrToken) {
        if (isEmpty(credsOrToken)) {
            return Optional.empty();
        }
        // check for token
        if (validateToken(credsOrToken)) {
            return Optional.of(credsOrToken);
        }
        // check password
        if (isValidCredsEntryPresentInFile(this.pwdFile, credsOrToken)) {
            // Gratz! Valid creds get a token!
            final String token = generateLoginToken();
            logins.put(token, System.currentTimeMillis());
            return Optional.of(token);
        }
        // no good
        return Optional.empty();
    }

    public boolean validateToken(final String token) {
        if (isEmpty(token)) {
            return false;
        }
        final Long millistamp = logins.get(token);
        return millistamp != null && !isExpired(millistamp, System.currentTimeMillis());
    }

    public void logoutToken(final String token) {
        if (isEmpty(token)) {
            return;
        }
        logins.remove(token);
    }

}
