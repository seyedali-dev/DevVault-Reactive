package com.dev.vault.service.module.mail;

import com.dev.vault.helper.exception.DevVaultException;
import com.dev.vault.helper.payload.email.Email;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@Slf4j
public class MailService {
    private final JavaMailSender mailSender;
    private final MailContentBuilder contentBuilder;

    @Async
    public void sendEmail(Email email) {
        MimeMessagePreparator mimeMessagePreparator = mimeMessage -> {
            MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage);
            messageHelper.setFrom("learn.mike.helloworlc@gmail.com");
            messageHelper.setSubject(email.getSubject());
            messageHelper.setTo(email.getRecipient());
            messageHelper.setText(contentBuilder.build(email.getBody()));
        };
        try {
            mailSender.send(mimeMessagePreparator);
            log.info("\n\n\t\tactivation email sent successfully");
        } catch (MailException e) {
            throw new DevVaultException("\n\n\t\tException occurred while sending mail to " + email.getRecipient());
        }
    }
}
