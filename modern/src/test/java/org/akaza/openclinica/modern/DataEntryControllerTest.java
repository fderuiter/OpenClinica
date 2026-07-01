package org.akaza.openclinica.modern;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DataEntryController.class)
@AutoConfigureMockMvc(addFilters = false)
public class DataEntryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void testDataEntryEndpoint() throws Exception {
        mockMvc.perform(get("/DataEntry"))
                .andExpect(status().isOk())
                .andExpect(content().string("Modern Data Entry Workflow"));
    }
}
