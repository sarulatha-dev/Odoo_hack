package com.brightloop.subscription.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.brightloop.subscription.config.AppProperties;

@Service
public class MailService {

    private static final Logger log = LoggerFactory.getLogger(MailService.class);

    private final JavaMailSender mailSender;
    private final AppProperties properties;

    public MailService(JavaMailSender mailSender, AppProperties properties) {
        this.mailSender = mailSender;
        this.properties = properties;
    }

    public void send(String to, String subject, String body) {
        if (!properties.mail().enabled()) {
            log.info("[mail:disabled] to={} subject={} body={}", to, subject, body);
            return;
        }
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(properties.mail().from());
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
            log.info("[mail:sent] to={} subject={}", to, subject);
        } catch (Exception ex) {
            log.warn("[mail:failed] to={} subject={} reason={}", to, subject, ex.getMessage());
        }
    }
}
