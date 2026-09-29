package org.example.notifyservice.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class EmailService {

    private final JavaMailSender mailSender;
    private final String from;

    public EmailService(
            JavaMailSender mailSender,
            @Value("${notification.mail.from}") String from) {
        this.mailSender = mailSender;
        this.from = from;
    }

    public void sendReservationCreatedEmail(String recipient) {
        String cleanedRecipient = StringUtils.trimWhitespace(recipient);
        if (!StringUtils.hasText(cleanedRecipient)) {
            throw new IllegalArgumentException("Recipient email is required");
        }

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(cleanedRecipient);
        message.setSubject("Đặt phòng thành công");
        message.setText("Cảm ơn bạn đã đặt phòng. Chúng tôi sẽ xử lý yêu cầu của bạn sớm.");

        mailSender.send(message);
    }
}
