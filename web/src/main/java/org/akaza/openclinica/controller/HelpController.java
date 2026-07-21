package org.akaza.openclinica.controller;

import org.akaza.openclinica.dao.core.CoreResources;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

@Controller
public class HelpController {

    private static final Logger logger = LoggerFactory.getLogger(HelpController.class);
    private Properties helpMappings;

    public HelpController() {
        helpMappings = new Properties();
        try (InputStream is = getClass().getResourceAsStream("/help_mapping.properties")) {
            if (is != null) {
                helpMappings.load(is);
            } else {
                logger.warn("Could not find help_mapping.properties");
            }
        } catch (IOException e) {
            logger.error("Error loading help_mapping.properties", e);
        }
    }

    @RequestMapping(value = "/help", method = RequestMethod.GET)
    public void resolveHelp(@RequestParam("url") String url, HttpServletResponse response) throws IOException {
        String targetUrl = url;
        
        if (url != null) {
            String mappedUrl = helpMappings.getProperty(url);
            if (mappedUrl != null) {
                if (mappedUrl.endsWith(".md")) {
                    mappedUrl = mappedUrl.substring(0, mappedUrl.length() - 3) + ".html";
                }
                
                String helpBaseUrl = CoreResources.getField("help.base.url");
                if (helpBaseUrl == null || helpBaseUrl.trim().isEmpty()) {
                    helpBaseUrl = "https://docs.openclinica.com";
                }
                
                String baseUrl = helpBaseUrl.trim();
                if (baseUrl.endsWith("/") && mappedUrl.startsWith("/")) {
                    targetUrl = baseUrl + mappedUrl.substring(1);
                } else if (!baseUrl.endsWith("/") && !mappedUrl.startsWith("/") && !baseUrl.isEmpty()) {
                    targetUrl = baseUrl + "/" + mappedUrl;
                } else {
                    targetUrl = baseUrl + mappedUrl;
                }
            }
        }
        
        response.sendRedirect(targetUrl);
    }
}
