package xyz.mattring.grambaal.ui.template.infra;

import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Supplier;

public class TemplateProcessingHandler<T> implements HttpHandler {

    private final Map<String, TemplateSpec<T>> templateMap;
    private final BiFunction<T, HttpServerExchange, String> templateRenderer;
    private final HttpHandler notFoundHandler;

    public TemplateProcessingHandler(BiFunction<T, HttpServerExchange, String> templateRenderer, HttpHandler notFoundHandler) {
        this.templateRenderer = templateRenderer;
        this.notFoundHandler = notFoundHandler;
        templateMap = new HashMap<>();
    }

    public void addTemplateSpec(TemplateSpec<T> templateSpec) {
        templateMap.put(templateSpec.getTemplateName(), templateSpec);
    }

    public void addTemplateSpec(String templateName, String templatePath, Supplier<T> contextSupplier) {
        addTemplateSpec(new TemplateSpec<>(templateName, templatePath, contextSupplier));
    }

    @Override
    public void handleRequest(HttpServerExchange httpServerExchange) throws Exception {
        final String path = httpServerExchange.getRequestPath();
        final String templateName = getLastPathSegment(path);
        if (templateName != null) {
            final TemplateSpec<T> templateSpec = templateMap.get(templateName);
            if (templateSpec != null) {
                final String contentType = templateSpec.getContentType();
                final String templatePath = templateSpec.getTemplatePath();
                final T templateContext = templateSpec.getContextSupplier().get();
                final String rendered = templateRenderer.apply(templateContext, httpServerExchange);
                httpServerExchange.getResponseHeaders().put(io.undertow.util.Headers.CONTENT_TYPE, contentType);
                httpServerExchange.getResponseSender().send(rendered);
                return;
            }
        }
        notFoundHandler.handleRequest(httpServerExchange);
    }

    public static String getLastPathSegment(String path) {
        // Remove any trailing slashes
        if (path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }
        // Get the last segment
        final int lastSlash = path.lastIndexOf('/');
        if (lastSlash > -1) {
            return path.substring(lastSlash + 1);
        }
        return null;
    }

}
