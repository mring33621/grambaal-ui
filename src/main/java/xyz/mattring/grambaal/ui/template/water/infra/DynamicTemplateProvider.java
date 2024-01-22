package xyz.mattring.grambaal.ui.template.water.infra;

import io.undertow.server.HttpServerExchange;
import org.watertemplate.Template;

import java.util.Deque;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class DynamicTemplateProvider {

    public static String generateKey() {
        return UUID.randomUUID().toString();
    }

    private final String queryParamKey;
    private final Map<String, Template> templateMap;

    public DynamicTemplateProvider(String queryParamKey) {
        this.templateMap = new ConcurrentHashMap<>();
        this.queryParamKey = queryParamKey;
    }

    public String getQueryParamKey() {
        return queryParamKey;
    }

    public String putTemplate(Template template, HttpServerExchange optionalExchange) {
        final String key = generateKey();
        templateMap.put(key, template);
        optionalExchange.addQueryParam(queryParamKey, key);
        return key;
    }

    public Template getTemplateOrDefault(HttpServerExchange exchange, Template defaultTemplate) {
        final Deque<String> keyDeque = exchange.getQueryParameters().get(queryParamKey);
        if (keyDeque == null || keyDeque.isEmpty()) {
            return defaultTemplate;
        }
        final String key = keyDeque.getFirst();
        return getTemplateOrDefault(key, defaultTemplate);
    }

    public Template getTemplateOrDefault(String key, Template defaultTemplate) {
        if (key == null) {
            return defaultTemplate;
        }
        Template template = templateMap.remove(key);
        if (template == null) {
            return defaultTemplate;
        }
        return template;
    }
}
