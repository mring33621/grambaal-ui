package xyz.mattring.grambaal.ui.template.infra;

import io.undertow.server.HttpServerExchange;

import java.util.function.Function;

public class TemplateSpec<T> {
    private final String templateName;
    private final String templatePath;
    private final String contentType;
    private final Function<HttpServerExchange, T> contextFunction;

    public TemplateSpec(String templateName, String templatePath, String contentType, Function<HttpServerExchange, T> contextFunction) {
        this.templateName = templateName;
        this.templatePath = templatePath;
        this.contentType = contentType != null ? contentType : "text/html";
        this.contextFunction = contextFunction;
    }

    public TemplateSpec(String templateName, String templatePath, Function<HttpServerExchange, T> contextFunction) {
        this(templateName, templatePath, null, contextFunction);
    }

    public String getTemplateName() {
        return templateName;
    }

    public String getTemplatePath() {
        return templatePath;
    }

    public String getContentType() {
        return contentType;
    }

    public Function<HttpServerExchange, T> getContextFunction() {
        return contextFunction;
    }

}
