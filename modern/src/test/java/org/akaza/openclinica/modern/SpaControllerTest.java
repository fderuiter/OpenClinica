package org.akaza.openclinica.modern;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.forwardedUrl;

@WebMvcTest(SpaController.class)
@AutoConfigureMockMvc(addFilters = false)
public class SpaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void testDataEntryEndpoint() throws Exception {
        mockMvc.perform(get("/DataEntry"))
                .andExpect(status().isOk())
                .andExpect(forwardedUrl("/index.html"));
    }

    @Test
    public void testCrfEndpoint() throws Exception {
        mockMvc.perform(get("/CRF/123"))
                .andExpect(status().isOk())
                .andExpect(forwardedUrl("/index.html"));
    }
}
