package org.akaza.openclinica.core.config;

import org.akaza.openclinica.core.OpenClinicaMailSender;
import org.akaza.openclinica.dao.core.CoreResources;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

import org.springframework.context.annotation.DependsOn;

@Configuration
public class EmailConfiguration {

    @Bean(name = "mailSender")
    @DependsOn("coreResources")
    public JavaMailSenderImpl mailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        
        mailSender.setHost(CoreResources.getField("mail.host"));
        mailSender.setUsername(CoreResources.getField("mail.username"));
        mailSender.setPassword(CoreResources.getField("mail.password"));
        
        String portStr = CoreResources.getField("mail.port");
        if (portStr != null && !portStr.isEmpty()) {
            try {
                mailSender.setPort(Integer.parseInt(portStr));
            } catch (NumberFormatException e) {
                // ignore
            }
        }
        
        String protocol = CoreResources.getField("mail.protocol");
        mailSender.setProtocol(protocol != null && !protocol.isEmpty() ? protocol : "smtp");
        
        Properties javaMailProperties = new Properties();
        javaMailProperties.setProperty("mail.smtp.auth", defaultIfEmpty(CoreResources.getField("mail.smtp.auth"), "false"));
        javaMailProperties.setProperty("mail.smtp.starttls.enable", defaultIfEmpty(CoreResources.getField("mail.smtp.starttls.enable"), "false"));
        javaMailProperties.setProperty("mail.smtps.auth", defaultIfEmpty(CoreResources.getField("mail.smtps.auth"), "false"));
        javaMailProperties.setProperty("mail.smtps.starttls.enable", defaultIfEmpty(CoreResources.getField("mail.smtps.starttls.enable"), "false"));
        javaMailProperties.setProperty("mail.smtp.connectiontimeout", defaultIfEmpty(CoreResources.getField("mail.smtp.connectiontimeout"), "10000"));
        
        javaMailProperties.setProperty("mail.smtp.starttls.required", defaultIfEmpty(CoreResources.getField("mail.smtp.starttls.required"), "false"));
        javaMailProperties.setProperty("mail.smtp.ssl.protocols", defaultIfEmpty(CoreResources.getField("mail.smtp.ssl.protocols"), "TLSv1.2"));
        
        mailSender.setJavaMailProperties(javaMailProperties);
        
        mailSender.setDefaultEncoding("UTF-8");
        
        return mailSender;
    }

    private String defaultIfEmpty(String value, String defaultValue) {
        return (value == null || value.isEmpty()) ? defaultValue : value;
    }

    @Bean(name = "openClinicaMailSender")
    public OpenClinicaMailSender openClinicaMailSender(JavaMailSenderImpl mailSender) {
        OpenClinicaMailSender sender = new OpenClinicaMailSender();
        sender.setMailSender(mailSender);
        return sender;
    }
}
