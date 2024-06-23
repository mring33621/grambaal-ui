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
        assertTrue(helper.getAvailableModelsAsOptions(GPTModel.DINFRA_LLAMA_3_70B.toString()).toString().contains("<option selected>" + GPTModel.DINFRA_LLAMA_3_70B.toString() + "</option>"));
        assertTrue(helper.getAvailableModelsAsOptions(GPTModel.GEM_1_5_PRO_LATEST.toString()).toString().contains("<option selected>" + GPTModel.GEM_1_5_PRO_LATEST.toString() + "</option>"));
    }

    @Test
    void findModelForModelString() {
        final UIGPTModelHelper helper = new UIGPTModelHelper(true);
        assertTrue(helper.findModelForModelString(GPTModel.GPT_4o.toString()).isPresent());
        assertTrue(helper.findModelForModelString(GPTModel.GEM_1_5_FLASH_LATEST.toString()).isPresent());
        assertFalse(helper.findModelForModelString("not a model").isPresent());
    }
}