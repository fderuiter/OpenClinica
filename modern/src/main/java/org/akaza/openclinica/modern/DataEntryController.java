package org.akaza.openclinica.modern;

import org.akaza.openclinica.sdk.dto.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DataEntryController {

    @GetMapping("/DataEntry")
    public ApiResponse<String> dataEntry() {
        return new ApiResponse<>("Modern Data Entry Workflow");
    }
}
