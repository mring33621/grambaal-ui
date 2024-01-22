package xyz.mattring.grambaal.ui.template.water.templates;

import org.watertemplate.Template;

public class HappyPath extends Template {

    public HappyPath(String message) {
        add("message", message);
    }

    @Override
    protected String getFilePath() {
        return "happy_path.html";
    }
}
