package xyz.mattring.grambaal.ui.template.water.templates;

import org.watertemplate.Template;
import xyz.mattring.grambaal.oai.GPTModel;

import java.util.List;
import java.util.Optional;

public class ConvoForm extends Template {

    private String sessionName, selectedModel, convoText, newEntryText;
    private final UIGPTModelHelper uiGPTModelHelper;

    public ConvoForm(String sessionName, String optionalSelectedModel, String convoText, String newEntryText) {
        uiGPTModelHelper = new UIGPTModelHelper();
        setSessionName(sessionName);
        if (!uiGPTModelHelper.isEmpty(optionalSelectedModel)) {
            setSelectedModel(optionalSelectedModel);
        }
        setConvoText(convoText);
        setNewEntryText(newEntryText);
        addCollection("availableModelOptions", getAvailableModelOptions());
    }

    @Override
    protected String getFilePath() {
        return "convo_form.html";
    }

    public String getSessionName() {
        return sessionName;
    }

    public void setSessionName(String sessionName) {
        this.sessionName = sessionName;
        add("sessionName", sessionName);
    }

    public String getSelectedModel() {
        return selectedModel;
    }

    public String getSelectedModelName() {
        return GPTModel.valueOf(selectedModel).getModelName();
    }

    public void setSelectedModel(String selectedModel) {
        this.selectedModel = selectedModel;
        add("selectedModel", selectedModel);
    }

    public String getConvoText() {
        return convoText;
    }

    public void setConvoText(String convoText) {
        this.convoText = convoText;
        add("convoText", convoText);
    }

    public String getNewEntryText() {
        return newEntryText;
    }

    public void setNewEntryText(String newEntryText) {
        this.newEntryText = newEntryText;
        add("newEntryText", newEntryText);
    }

    private List<String> getAvailableModelOptions() {
        return uiGPTModelHelper.getAvailableModelsAsOptions(selectedModel);
    }

    public Optional<GPTModel> findModelForModelString(String modelString) {
        return uiGPTModelHelper.findModelForModelString(modelString);
    }
}
