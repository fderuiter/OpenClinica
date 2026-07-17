package org.akaza.openclinica.modern;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class AccountUiController {

    @GetMapping("/ListUserAccounts")
    public String listUserAccounts() {
        return "forward:/index.html";
    }

    @GetMapping("/CreateUserAccount")
    public String createUserAccount() {
        return "forward:/index.html";
    }

    @GetMapping("/EditUserAccount")
    public String editUserAccount() {
        return "forward:/index.html";
    }

    @GetMapping("/ViewUserAccount")
    public String viewUserAccount() {
        return "forward:/index.html";
    }
}
