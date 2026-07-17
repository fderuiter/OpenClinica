package org.akaza.openclinica.modern;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class SpaController {

    @RequestMapping({
        "/DataEntry",
        "/DataEntry/**",
        "/CRF",
        "/CRF/**"
    })
    public String index() {
        return "forward:/index.html";
    }
}
