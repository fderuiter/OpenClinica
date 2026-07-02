package org.akaza.openclinica.modern;

import com.atlassian.oai.validator.OpenApiInteractionValidator;
import com.atlassian.oai.validator.mockmvc.OpenApiValidationMatchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class ContractTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void testDataEntryContract() throws Exception {
        String openapiJson = mockMvc.perform(get("/v3/api-docs"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        OpenApiInteractionValidator validator = OpenApiInteractionValidator
                .createForInlineApiSpecification(openapiJson)
                .build();
        
        mockMvc.perform(get("/DataEntry"))
               .andExpect(status().isOk())
               .andExpect(OpenApiValidationMatchers.openApi().isValid(validator));
    }
}
