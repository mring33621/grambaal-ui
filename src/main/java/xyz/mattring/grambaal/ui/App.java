package xyz.mattring.grambaal.ui;

import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.RoutingHandler;
import io.undertow.server.handlers.Cookie;
import io.undertow.server.handlers.CookieImpl;
import io.undertow.server.handlers.PathHandler;
import io.undertow.server.handlers.form.FormData;
import io.undertow.server.handlers.form.FormDataParser;
import io.undertow.server.handlers.form.FormParserFactory;
import io.undertow.server.handlers.resource.ClassPathResourceManager;
import io.undertow.server.handlers.resource.ResourceHandler;
import io.undertow.util.Headers;
import io.undertow.util.StatusCodes;
import org.watertemplate.Template;
import xyz.mattring.grambaal.GPTSessionInteractor;
import xyz.mattring.grambaal.ui.template.infra.DecisionHandler;
import xyz.mattring.grambaal.ui.template.infra.TemplateProcessingHandler;
import xyz.mattring.grambaal.ui.template.water.infra.DynamicTemplateProvider;
import xyz.mattring.grambaal.ui.template.water.templates.ConvoForm;
import xyz.mattring.grambaal.ui.template.water.templates.HappyPath;
import xyz.mattring.grambaal.ui.users.UsrMgt;

import java.io.File;
import java.nio.file.Files;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Predicate;

public class App {

    public static final String APP_NAME = "grambaal-ui";
    public static final String TOKEN_COOKIE_NAME = APP_NAME + "-token";
    public static final String TEXT_PLAIN = "text/plain";
    public static final String TEXT_HTML = "text/html";

    public static void notFoundHandler(HttpServerExchange exchange) {
        exchange.setStatusCode(404);
        exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, TEXT_PLAIN);
        exchange.getResponseSender().send("404: Page Not Found :(");
    }

    public static void doubleSecretProbationHandler(HttpServerExchange exchange) {
        exchange.setStatusCode(404);
        exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, TEXT_PLAIN);
        exchange.getResponseSender().send("404: Page Not Found :)");
    }


    private final String basePath;
    private final UsrMgt usrMgt;
    private final FormParserFactory formParserFactory;
    private final DynamicTemplateProvider dynamicTemplateProvider;

    public App(String basePath) {
        this.basePath = basePath;
        this.usrMgt = new UsrMgt(APP_NAME);
        this.formParserFactory = FormParserFactory.builder().withDefaultCharset("UTF-8").build();
        this.dynamicTemplateProvider = new DynamicTemplateProvider("grambaal-tkey");
    }

    boolean isProvisioned() {
        return usrMgt.isProvisioned();
    }

    void signalTempRedirect(HttpServerExchange exchange, String targetLocation) {
        String adjustedLocation = isEmpty(basePath) ? targetLocation : basePath + targetLocation;
        exchange.setStatusCode(StatusCodes.FOUND);
        exchange.getResponseHeaders().put(Headers.LOCATION, adjustedLocation);
        exchange.endExchange();
    }

    public void helloHandler(HttpServerExchange exchange) {
        exchange.setStatusCode(200);
        exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, TEXT_PLAIN);
        Cookie tokenCookie = exchange.getRequestCookie(TOKEN_COOKIE_NAME);
        String helloResponse = "Hello World!";
        if (tokenCookie != null) {
            helloResponse += " Your token is: " + tokenCookie.getValue();
            boolean isValid = usrMgt.validateToken(tokenCookie.getValue());
            helloResponse += " It is " + (isValid ? "valid" : "invalid");
        } else {
            helloResponse += " You have no token.";
        }
        exchange.getResponseSender().send(helloResponse);
    }

    public void goodbyeHandler(HttpServerExchange exchange) {
        exchange.setStatusCode(200);
        exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, TEXT_PLAIN);
        exchange.getResponseSender().send("Goodbye World!");
    }

    public void supplyGptConvoForm(HttpServerExchange exchange) {
        exchange.setStatusCode(200);
        exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, TEXT_PLAIN);
        exchange.getResponseSender().send("I'm the GPT Convo Form!");
    }

    public void login(HttpServerExchange exchange) {
        exchange.setStatusCode(200);
        exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, TEXT_PLAIN);
        exchange.getResponseSender().send("You're trying to log in!");
    }

    public void processGptConvoChunk(HttpServerExchange exchange) {
        exchange.setStatusCode(200);
        exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, TEXT_PLAIN);
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
            newLocation = "/t/ConvoForm";
            final Cookie tokenCookie = new CookieImpl(TOKEN_COOKIE_NAME, token.get());
            tokenCookie.setPath("/");
            exchange.setResponseCookie(tokenCookie);
        }
        signalTempRedirect(exchange, newLocation);
    }

    static boolean isEmpty(String s) {
        return s == null || s.isEmpty();
    }

    static boolean isPlaceHolder(String s) {
        return isEmpty(s) || s.startsWith("New");
    }

    void processNewEntry(HttpServerExchange exchange) {
        try (FormDataParser parser = formParserFactory.createParser(exchange)) {
            parser.parse(exch -> {
                final FormData data = exchange.getAttachment(FormDataParser.FORM_DATA);
                final String sessionName = data.getFirst("sessionName").getValue();
                final String[] newEntry = {data.getFirst("newEntry").getValue()};
                final String[] convoText = {""};

                if (isPlaceHolder(newEntry[0]) && !isPlaceHolder(sessionName)) {
                    convoText[0] = GPTSessionInteractor.getConvoTextForSession(sessionName)
                            .orElse("No convo found for: " + sessionName);
                }

                if (!isPlaceHolder(newEntry[0]) && !isPlaceHolder(sessionName)) {
                    final File tempFile = File.createTempFile("newUserPrompt", ".txt");
                    Files.writeString(tempFile.toPath(), newEntry[0] + "\n");
                    GPTSessionInteractor gptSessionInteractor = new GPTSessionInteractor(sessionName, tempFile.getAbsolutePath(), "gpt-4");
                    gptSessionInteractor.run();
                    convoText[0] = GPTSessionInteractor.getConvoTextForSession(sessionName)
                            .orElse("No convo found for: " + sessionName);
                    newEntry[0] = "";
                    tempFile.delete();
                }

                final Template template = new ConvoForm(
                        sessionName,
                        convoText[0],
                        newEntry[0]);
                final String key = dynamicTemplateProvider.putTemplate(template, exchange);
                signalTempRedirect(exchange, "/t/ConvoForm?" + dynamicTemplateProvider.getQueryParamKey() + "=" + key);
            });
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    TemplateProcessingHandler<Template> getTemplateProcessingHandler() {
        final BiFunction<Template, HttpServerExchange, String> waterTemplateRenderer = (template, exchange) -> {
            return template.render();
        };
        final TemplateProcessingHandler<Template> templateHandler = new TemplateProcessingHandler<>(
                waterTemplateRenderer,
                App::notFoundHandler
        );
        templateHandler.addTemplateSpec(
                "HappyPath",
                "unused",
                (exch) -> new HappyPath("Hello, World!"));
        templateHandler.addTemplateSpec(
                "ConvoForm",
                "unused",
                (exch) -> {
                    final Template def = new ConvoForm("NewSession", "ConvoPlaceholder", "NewEntryPlaceholder");
                    return dynamicTemplateProvider.getTemplateOrDefault(exch, def);
                });
        return templateHandler;
    }


    public HttpHandler getCompositeHandler() {
        final ResourceHandler staticFileHandler = new ResourceHandler(
                new ClassPathResourceManager(Main.class.getClassLoader()));

        final Predicate<HttpServerExchange> isTokenValid = exch -> {
            final Cookie tokenCookie = exch.getRequestCookie(TOKEN_COOKIE_NAME);
            return tokenCookie != null && usrMgt.validateToken(tokenCookie.getValue());
        };
        final HttpHandler guardedTemplateHandler = new DecisionHandler(
                getTemplateProcessingHandler(),
                App::doubleSecretProbationHandler,
                isTokenValid
        );

        final HttpHandler otherHandler = new RoutingHandler()
                .post("/trylogin", this::tryLoginHandler)
                .post("/newEntry", this::processNewEntry)
                .get("/hello", this::helloHandler)
                .get("/goodbye", this::goodbyeHandler)
                .get("/convo", this::supplyGptConvoForm)
                .post("/convo", this::processGptConvoChunk)
                .setFallbackHandler(App::notFoundHandler);

        final HttpHandler welcomeHandler = exch -> {
            signalTempRedirect(exch, "/s/pages/login.html");
        };

        final PathHandler compositeHandler = new PathHandler()
                .addExactPath("/", welcomeHandler)
                .addPrefixPath("/s", staticFileHandler)
                .addPrefixPath("/t", guardedTemplateHandler)
                .addPrefixPath("/o", otherHandler);

        return compositeHandler;
    }

}
