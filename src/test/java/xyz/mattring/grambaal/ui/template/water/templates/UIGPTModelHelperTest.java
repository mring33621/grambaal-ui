package xyz.mattring.grambaal.ui.template.water.templates;

import org.junit.jupiter.api.Test;
import xyz.mattring.grambaal.oai.GPTModel;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UIGPTModelHelperTest {

    @Test
    void isEmpty() {
        assertTrue(UIGPTModelHelper.isEmpty(null));
        assertTrue(UIGPTModelHelper.isEmpty(""));
        assertFalse(UIGPTModelHelper.isEmpty("not empty"));
    }

    @Test
    void getAvailableModelsAsOptions() {
        final UIGPTModelHelper helper = new UIGPTModelHelper();
        assertFalse(helper.getAvailableModelsAsOptions(null).toString().contains("selected"));
        assertTrue(helper.getAvailableModelsAsOptions(GPTModel.GPT_4_VISION_PREVIEW.toString()).toString().contains("<option selected>" + GPTModel.GPT_4_VISION_PREVIEW.toString() + "</option>"));
        assertTrue(helper.getAvailableModelsAsOptions(GPTModel.GPT_3_5_TURBO_INSTRUCT.toString()).toString().contains("<option selected>" + GPTModel.GPT_3_5_TURBO_INSTRUCT.toString() + "</option>"));
    }

    @Test
    void findModelForModelString() {
        final UIGPTModelHelper helper = new UIGPTModelHelper();
        assertTrue(helper.findModelForModelString(GPTModel.GPT_4_VISION_PREVIEW.toString()).isPresent());
        assertTrue(helper.findModelForModelString(GPTModel.GPT_3_5_TURBO_INSTRUCT.toString()).isPresent());
        assertFalse(helper.findModelForModelString("not a model").isPresent());
    }
}