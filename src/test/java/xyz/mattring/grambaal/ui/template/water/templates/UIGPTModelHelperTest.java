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
        final UIGPTModelHelper helper = new UIGPTModelHelper(false);
        assertFalse(helper.getAvailableModelsAsOptions(null).toString().contains("selected"));
        assertTrue(helper.getAvailableModelsAsOptions(GPTModel.GPT_4_VISION_PREVIEW.toString()).toString().contains("<option selected>" + GPTModel.GPT_4_VISION_PREVIEW.toString() + "</option>"));
        assertTrue(helper.getAvailableModelsAsOptions(GPTModel.GEM_PRO.toString()).toString().contains("<option selected>" + GPTModel.GEM_PRO.toString() + "</option>"));
    }

    @Test
    void findModelForModelString() {
        final UIGPTModelHelper helper = new UIGPTModelHelper(true);
        assertTrue(helper.findModelForModelString(GPTModel.GPT_4_VISION_PREVIEW.toString()).isPresent());
        assertTrue(helper.findModelForModelString(GPTModel.DINFRA_YI_34B.toString()).isPresent());
        assertFalse(helper.findModelForModelString("not a model").isPresent());
    }
}