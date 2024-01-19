package xyz.mattring.grambaal.ui;

import io.undertow.Undertow;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.RoutingHandler;
import io.undertow.server.handlers.PathHandler;
import io.undertow.server.handlers.resource.ClassPathResourceManager;
import io.undertow.server.handlers.resource.ResourceHandler;
import io.undertow.util.Headers;
import xyz.mattring.grambaal.ui.undertow.ResourceManagerWrapper;
import xyz.mattring.grambaal.ui.undertow.ResourceWrapper;
import xyz.mattring.grambaal.ui.undertow.SenderWrapper;

import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.UnaryOperator;

public class Main implements Runnable {

    static final String PASSWORD_FILE = System.getProperty("user.home") + "/gu.txt";
    static final int FIVE_MINUTES = 5 * 60 * 1000;

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

    public static void notFoundHandler(HttpServerExchange exchange) {
        exchange.setStatusCode(404);
        exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/plain");
        exchange.getResponseSender().send("Page Not Found");
    }

    private final int port;
    private final Map<String, Long> logins;

    public Main(int port) {
        this.port = port;
        this.logins = new java.util.concurrent.ConcurrentHashMap<>();
        final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
        executorService.scheduleAtFixedRate(
                this::removeExpiredLogins,
                1, 1, java.util.concurrent.TimeUnit.MINUTES);
        Runtime.getRuntime().addShutdownHook(new Thread(executorService::shutdown));
    }

    void removeExpiredLogins() {
        logins.entrySet().removeIf(e -> isExpired(e.getValue(), System.currentTimeMillis()));
    }

    Optional<String> tryLogin(final String pwdOrToken) {
        // check token
        final Long millistamp = logins.get(pwdOrToken);
        if (millistamp != null) {
            // token was good
            return Optional.of(pwdOrToken);
        }
        // check password
        final String lastLine = readLastLineFromFile(PASSWORD_FILE);
        if (pwdOrToken.equals(lastLine)) {
            // password was good
            final String token = generateLoginToken();
            logins.put(token, System.currentTimeMillis());
            return Optional.of(token);
        }
        // no good
        return Optional.empty();
    }

    public void helloHandler(HttpServerExchange exchange) {
        exchange.setStatusCode(200);
        exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/plain");
        exchange.getResponseSender().send("Hello World!");
    }

    public void goodbyeHandler(HttpServerExchange exchange) {
        exchange.setStatusCode(200);
        exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/plain");
        exchange.getResponseSender().send("Goodbye World!");
    }

    public void supplyGptConvoForm(HttpServerExchange exchange) {
        exchange.setStatusCode(200);
        exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/plain");
        exchange.getResponseSender().send("I'm the GPT Convo Form!");
    }

    public void login(HttpServerExchange exchange) {
        exchange.setStatusCode(200);
        exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/plain");
        exchange.getResponseSender().send("You're trying to log in!");
    }

    public void processGptConvoChunk(HttpServerExchange exchange) {
        exchange.setStatusCode(200);
        exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/plain");
        exchange.getResponseSender().send("I have processed your GPT Convo Chunk!");
    }

    @Override
    public void run() {
        final ResourceHandler staticFileHandler = new ResourceHandler(
                new ClassPathResourceManager(Main.class.getClassLoader()));
        final HttpHandler routingHandler = new RoutingHandler()
                .get("/hello", this::helloHandler)
                .get("/goodbye", this::goodbyeHandler)
                .get("/gsession", this::supplyGptConvoForm)
                .post("/gsession", this::processGptConvoChunk)
                .setFallbackHandler(Main::notFoundHandler);

        final UnaryOperator<ByteBuffer> templateProcessor = byteBuffer -> {
            // currently just reverses a string, which is a placeholder for the real template processing
            // TODO: actually process the template
            String orig = new String(byteBuffer.array()).trim();
            String reversed = new StringBuilder(orig).reverse().toString();
            System.out.println("orig: " + orig);
            System.out.println("reversed: " + reversed);
            return ByteBuffer.wrap(reversed.getBytes());
        };
        final ResourceHandler templateHandler = new ResourceHandler(
                new ResourceManagerWrapper(
                        new ClassPathResourceManager(Main.class.getClassLoader()),
                        resource -> new ResourceWrapper(
                                resource,
                                sender -> new SenderWrapper(
                                        sender,
                                        templateProcessor
                                )
                        )
                )
        );

        final PathHandler compositeHandler = new PathHandler()
                .addPrefixPath("/s", staticFileHandler)
                .addPrefixPath("/d", templateHandler)
                .addPrefixPath("/x", routingHandler);
        Undertow server = Undertow.builder()
                .addHttpListener(port, "localhost")
                .setHandler(compositeHandler)
                .build();
        server.start();
    }

    public static void main(String[] args) {
        // die if no password file
        if (readLastLineFromFile(PASSWORD_FILE) == null) {
            System.err.println("No password file found. Exiting.");
            System.exit(1);
        }
        int port = Integer.parseInt(args[0]);
        new Main(port).run();
    }
}