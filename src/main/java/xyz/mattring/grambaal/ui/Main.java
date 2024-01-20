package xyz.mattring.grambaal.ui;

import io.undertow.Undertow;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.RoutingHandler;
import io.undertow.server.handlers.PathHandler;
import io.undertow.server.handlers.form.FormData;
import io.undertow.server.handlers.form.FormDataParser;
import io.undertow.server.handlers.form.FormParserFactory;
import io.undertow.server.handlers.resource.ClassPathResourceManager;
import io.undertow.server.handlers.resource.ResourceHandler;
import io.undertow.util.Headers;
import io.undertow.util.StatusCodes;
import org.watertemplate.Template;
import xyz.mattring.grambaal.ui.template.infra.TemplateProcessingHandler;
import xyz.mattring.grambaal.ui.template.water.HappyPath;
import xyz.mattring.grambaal.ui.users.UsrMgt;

import java.util.Optional;
import java.util.function.BiFunction;

public class Main implements Runnable {

    public static void notFoundHandler(HttpServerExchange exchange) {
        exchange.setStatusCode(404);
        exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/plain");
        exchange.getResponseSender().send("Page Not Found");
    }

    void signalTempRedirect(HttpServerExchange exchange, String newLocation) {
        exchange.setStatusCode(StatusCodes.FOUND);
        exchange.getResponseHeaders().put(Headers.LOCATION, newLocation);
        exchange.endExchange();
    }

    private final int port;
    private UsrMgt usrMgt;
    private final FormParserFactory formParserFactory;

    public Main(int port) {
        this.port = port;
        this.usrMgt = new UsrMgt("grambaal-ui");
        this.formParserFactory = FormParserFactory.builder().withDefaultCharset("UTF-8").build();
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

    public void tryLoginHandler(HttpServerExchange exchange) {
        try (FormDataParser parser = formParserFactory.createParser(exchange)) {
            parser.parse(exch -> handleLoginForm(exchange));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    void handleLoginForm(HttpServerExchange exchange) {
        FormData data = exchange.getAttachment(FormDataParser.FORM_DATA);
        String username = data.getFirst("username").getValue();
        String password = data.getFirst("pwd").getValue();
        Optional<String> token = usrMgt.tryLogin(username + "|" + password);
        String newLocation = "/o/goodbye";
        if (token.isPresent()) {
            newLocation = "/o/hello";
        }
        signalTempRedirect(exchange, newLocation);
    }

    @Override
    public void run() {

        final ResourceHandler staticFileHandler = new ResourceHandler(
                new ClassPathResourceManager(Main.class.getClassLoader()));

        final TemplateProcessingHandler<Template> templateHandler = getTemplateProcessingHandler();

        final HttpHandler otherHandler = new RoutingHandler()
                .post("/trylogin", this::tryLoginHandler)
                .get("/hello", this::helloHandler)
                .get("/goodbye", this::goodbyeHandler)
                .get("/gsession", this::supplyGptConvoForm)
                .post("/gsession", this::processGptConvoChunk)
                .setFallbackHandler(Main::notFoundHandler);

        final HttpHandler welcomeHandler = exch -> {
            signalTempRedirect(exch, "/s/pages/login.html");
        };

        final PathHandler compositeHandler = new PathHandler()
                .addExactPath("/", welcomeHandler)
                .addPrefixPath("/s", staticFileHandler)
                .addPrefixPath("/t", templateHandler)
                .addPrefixPath("/o", otherHandler);

        Undertow server = Undertow.builder()
                .addHttpListener(port, "localhost")
                .setHandler(compositeHandler)
                .build();
        server.start();
    }

    private static TemplateProcessingHandler<Template> getTemplateProcessingHandler() {
        final BiFunction<Template, HttpServerExchange, String> waterTemplateRenderer = (template, exchange) -> {
            return template.render();
        };
        final TemplateProcessingHandler<Template> templateHandler = new TemplateProcessingHandler<>(
                waterTemplateRenderer,
                Main::notFoundHandler
        );
        templateHandler.addTemplateSpec(
                "HappyPath",
                "unused",
                () -> new HappyPath("Hello, World!"));
        return templateHandler;
    }

    public static void main(String[] args) {
        final int port = Integer.parseInt(args[0]);
        final Main main = new Main(port);
        if (!main.usrMgt.isProvisioned()) {
            System.err.println("UsrMgt not ready. Exiting.");
            System.exit(1);
        }
        main.run();
    }
}