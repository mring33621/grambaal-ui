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
        assertTrue(helper.getAvailableModelsAsOptions(GPTModel.CLAUDE_3_5_HAIKU.toString()).toString().contains("<option selected>" + GPTModel.CLAUDE_3_5_HAIKU + "</option>"));
        assertTrue(helper.getAvailableModelsAsOptions(GPTModel.GEM_2_0_FLASH_LATEST.toString()).toString().contains("<option selected>" + GPTModel.GEM_2_0_FLASH_LATEST + "</option>"));
    }

    @Test
    void findModelForModelString() {
        final UIGPTModelHelper helper = new UIGPTModelHelper(true);
        assertTrue(helper.findModelForModelString(GPTModel.GPT_o3_mini.toString()).isPresent());
        assertTrue(helper.findModelForModelString(GPTModel.MISTRAL_CODESTRAL.toString()).isPresent());
        assertFalse(helper.findModelForModelString("not a model").isPresent());
    }
}