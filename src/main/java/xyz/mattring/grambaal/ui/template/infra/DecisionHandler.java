package xyz.mattring.grambaal.ui.template.infra;

import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;

import java.util.function.Predicate;

public class DecisionHandler implements HttpHandler {

    private final HttpHandler yesHandler;
    private final HttpHandler noHandler;
    private final Predicate<HttpServerExchange> decisionPredicate;

    public DecisionHandler(HttpHandler yesHandler, HttpHandler noHandler, Predicate<HttpServerExchange> decisionPredicate) {
        this.yesHandler = yesHandler;
        this.noHandler = noHandler;
        this.decisionPredicate = decisionPredicate;
    }

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        if (decisionPredicate.test(exchange)) {
            yesHandler.handleRequest(exchange);
        } else {
            noHandler.handleRequest(exchange);
        }
    }
}
