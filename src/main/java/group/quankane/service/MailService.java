package group.quankane.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class MailService {

    private final JavaMailSender mailSender;

    private final SpringTemplateEngine templateEngine;

    private static final String EMAIL_FROM_REPLACE = "Quân Kane";

    @Value("${spring.mail.from}")
    private String emailFrom;

    @Value("${endpoint.confirmUser}")
    private String apiConfirmUser;

    /*
    * Send email by Google SMTP
    *
    * @param recipients
    * @param subject
    * @param content
    * @param files
    * @return
    * @throws MessagingException
    * @throws UnsupportedEncodingException
    * */
    public String sendMail (String recipients, String subject, String content, MultipartFile[] files) throws MessagingException, UnsupportedEncodingException {
        log.info("Sending mail...");

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        helper.setFrom(emailFrom, EMAIL_FROM_REPLACE);

        if (recipients.contains(",")) {
            helper.setTo(InternetAddress.parse(recipients));
        } else {
            helper.setTo(recipients);
        }

        //Send attach file
        if (files != null) {
            for (MultipartFile file : files) {
                helper.addAttachment(file.getOriginalFilename(), file);
            }
        }

        helper.setSubject(subject);
        helper.setText(content);
        mailSender.send(message);

        log.info("Email has sent to successfully, recipients: {}", recipients);

        return "Sent";
    }

    /*
    * Send link confirm to email register
    *
    * @param emailTo
    * @param userId
    * @param verifyCode
    * @throws MessagingException
    * @throws UnsupportedEncodingException*/
    public void sendConfirmLink (String emailTo, Long userId, String verifyCode) throws MessagingException, UnsupportedEncodingException {
        log.info("Sending confirming link to user, email={}", emailTo);

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED, StandardCharsets.UTF_8.name());
        Context context = new Context();

        // http://localhost:80/user/confirm/{userId}?verifyCode={verifyCode}
        String linkConfirm = String.format("%s/%s?verifyCode=%s", apiConfirmUser, userId, verifyCode);

        Map<String, Object> properties = new HashMap<>();
        properties.put("linkConfirm", linkConfirm);
        context.setVariables(properties);

        helper.setFrom(emailFrom, EMAIL_FROM_REPLACE);
        helper.setTo(emailTo);
        helper.setSubject("Please confirm your account");

        String html = templateEngine.process("confirm-email.html", context);
        helper.setText(html, true);

        mailSender.send(message);
        log.info("Confirming link has sent to user, email={}, linkConfirm={}", emailTo, linkConfirm);
    }

    /*
     * Send link confirm to email register by Kafka
     *
     * @param message
     * @throws MessagingException
     * @throws UnsupportedEncodingException*/
    @KafkaListener(topics = "confirm-account-topic", groupId = "confirm-account-group")
    public void sendConfirmLinkByKafka (String message) throws MessagingException, UnsupportedEncodingException {
        log.info("Sending confirming link to user, message={}", message);

        String[] arr = message.split(",");
        String emailTo = arr[0].substring(arr[0].indexOf("=") + 1);
        String userId = arr[1].substring(arr[1].indexOf("=") + 1);
        String verifyCode = arr[2].substring(arr[2].indexOf("=") + 1);

        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED, StandardCharsets.UTF_8.name());
        Context context = new Context();

        // http://localhost:80/user/confirm/{userId}?verifyCode={verifyCode}
        String linkConfirm = String.format("%s/%s?verifyCode=%s", apiConfirmUser, userId, verifyCode);

        Map<String, Object> properties = new HashMap<>();
        properties.put("linkConfirm", linkConfirm);
        context.setVariables(properties);

        helper.setFrom(emailFrom, EMAIL_FROM_REPLACE);
        helper.setTo(emailTo);
        helper.setSubject("Please confirm your account");

        String html = templateEngine.process("confirm-email.html", context);
        helper.setText(html, true);

        mailSender.send(mimeMessage);
        log.info("Confirming link has sent to user, email={}, linkConfirm={}", emailTo, linkConfirm);
    }
}
