package xyz.mattring.grambaal.ui.template.infra;

import java.util.function.Supplier;

public class TemplateSpec<T> {
    private final String templateName;
    private final String templatePath;
    private final String contentType;
    private final Supplier<T> contextSupplier;

    public TemplateSpec(String templateName, String templatePath, String contentType, Supplier<T> contextSupplier) {
        this.templateName = templateName;
        this.templatePath = templatePath;
        this.contentType = contentType != null ? contentType : "text/html";
        this.contextSupplier = contextSupplier;
    }

    public TemplateSpec(String templateName, String templatePath, Supplier<T> contextSupplier) {
        this(templateName, templatePath, null, contextSupplier);
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

    public Supplier<T> getContextSupplier() {
        return contextSupplier;
    }

}
