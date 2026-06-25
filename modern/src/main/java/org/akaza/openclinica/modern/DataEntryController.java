package org.akaza.openclinica.modern;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DataEntryController {

    @GetMapping("/DataEntry")
    public String dataEntry() {
        return "Modern Data Entry Workflow";
    }
}
