package org.akaza.openclinica.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.HashMap;

@Controller
@RequestMapping(value = "/healthcheck")
public class HealthCheckController {

    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<HashMap<String, String>> healthCheck() {
        HashMap<String, String> response = new HashMap<>();
        response.put("status", "UP");
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
