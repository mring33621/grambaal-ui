package xyz.mattring.grambaal.ui.template.water.templates;

import org.watertemplate.Template;
import xyz.mattring.grambaal.GPTSessionInteractor;
import xyz.mattring.grambaal.oai.GPTModel;
import xyz.mattring.grambaal.ui.App;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.StringJoiner;

public class ConvoForm extends Template {

    private String sessionName, selectedModel, convoText, newEntryText;
    private final UIGPTModelHelper uiGPTModelHelper;

    public ConvoForm(String sessionName, String optionalSelectedModel, String convoText, String newEntryText) {
        uiGPTModelHelper = new UIGPTModelHelper(true);
        setSessionName(sessionName);
        setSelectedModel(optionalSelectedModel);
        setConvoText(convoText);
        setNewEntryText(newEntryText);
    }

    @Override
    protected String getFilePath() {
        return "convo_form.html";
    }

    public String getSessionName() {
        return sessionName;
    }

    public void setSessionName(final String sessionName) {
        this.sessionName = sessionName;
        add("sessionName", sessionName);
        // TODO: move the existing sessions stuff elsewhere?
        List<String> availableSessionOptions = Collections.emptyList();
        try {
            List<String> availableSessions =
                    GPTSessionInteractor.getExistingSessions(GPTSessionInteractor.SESSION_BASEDIR);
            availableSessionOptions =
                    availableSessions.stream()
                            .map(s -> s.equals(sessionName) ? "<option selected>" + s + "</option>" : "<option>" + s + "</option>")
                            .toList();
        } catch (Exception e) {
            e.printStackTrace();
        }
        addCollection("availableSessionOptions", availableSessionOptions);
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
        addCollection("availableModelOptions", uiGPTModelHelper.getAvailableModelsAsOptions(selectedModel));
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
        final String trimmedNewEntryText = App.trimIfPresent(newEntryText);
        this.newEntryText = trimmedNewEntryText;
        add("newEntryText", trimmedNewEntryText);
    }

    public Optional<GPTModel> findModelForModelString(String modelString) {
        return uiGPTModelHelper.findModelForModelString(modelString);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", ConvoForm.class.getSimpleName() + "[", "]")
                .add("sessionName='" + sessionName + "'")
                .add("selectedModel='" + selectedModel + "'")
                .add("newEntryText='" + newEntryText + "'")
                .toString();
    }
}
