package xyz.mattring.grambaal.ui.template.water.templates;

import xyz.mattring.grambaal.oai.GPTModel;
import xyz.mattring.grambaal.oai.GPTModelHelper;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class UIGPTModelHelper {

    public static boolean isEmpty(String s) {
        return s == null || s.isBlank();
    }

    private final GPTModelHelper gptModelHelper;

    public UIGPTModelHelper(boolean excludeModelsWithoutApiKeys) {
        gptModelHelper = new GPTModelHelper(excludeModelsWithoutApiKeys);
    }

    public List<String> getAvailableModelsAsOptions(String selectedModel) {
        final boolean hasSelectedModel = !isEmpty(selectedModel);
        return gptModelHelper.getSortedModels().stream()
                .map(GPTModel::toString)
                .map(m -> (hasSelectedModel && m.equals(selectedModel)) ? "<option selected>" + m + "</option>" : "<option>" + m + "</option>")
                .toList();
    }

    public Optional<GPTModel> findModelForModelString(String modelString) {
        return Arrays.stream(GPTModel.values())
                .filter(model -> model.toString().equals(modelString))
                .findFirst();
    }
}
