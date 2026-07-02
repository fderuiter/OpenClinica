package org.akaza.openclinica.modern;

import com.atlassian.oai.validator.mockmvc.OpenApiValidationMatchers;
import org.akaza.openclinica.modern.model.ConfigurationDraft;
import org.akaza.openclinica.modern.service.ConfigurationDraftService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ConfigurationDraftController.class)
public class ConfigurationDraftControllerContractIT {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ConfigurationDraftService draftService;

    @Test
    public void testGetDraftMatchesContract() throws Exception {
        ConfigurationDraft draft = new ConfigurationDraft();
        draft.setId("123");
        draft.setUserName("testUser");
        draft.setDraftType("DATASET");
        draft.setDraftData("{\"key\":\"value\"}");
        draft.setCreatedAt(new java.util.Date());
        draft.setExpiresAt(new java.util.Date());

        when(draftService.getDraft(anyString())).thenReturn(draft);

        mockMvc.perform(get("/api/config/draft/123")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(OpenApiValidationMatchers.openApi().isValid("/app/modern/target/openapi.json"));
    }
}
