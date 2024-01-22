package xyz.mattring.grambaal.ui.template.water.templates;

import org.watertemplate.Template;

public class ConvoForm extends Template {

    private String sessionName;
    private String convoText;
    private String newEntryText;

    public ConvoForm() {
    }

    public ConvoForm(String sessionName, String convoText, String newEntryText) {
        this.sessionName = sessionName;
        this.convoText = convoText;
        this.newEntryText = newEntryText;
        fillInFormData();
    }

    @Override
    protected String getFilePath() {
        return "convo_form.html";
    }

    public void fillInFormData() {
        clearFormData();
        add("sessionName", sessionName);
        add("convoText", convoText);
        add("newEntryText", newEntryText);
    }

    public void clearFormData() {
        add("sessionName", "");
        add("convoText", "");
        add("newEntryText", "");
    }

    public String getSessionName() {
        return sessionName;
    }

    public void setSessionName(String sessionName) {
        this.sessionName = sessionName;
    }

    public String getConvoText() {
        return convoText;
    }

    public void setConvoText(String convoText) {
        this.convoText = convoText;
    }

    public String getNewEntryText() {
        return newEntryText;
    }

    public void setNewEntryText(String newEntryText) {
        this.newEntryText = newEntryText;
    }
}
