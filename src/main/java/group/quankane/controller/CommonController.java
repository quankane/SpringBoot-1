package group.quankane.controller;

import group.quankane.dto.response.ResponseData;
import group.quankane.dto.response.ResponseError;
import group.quankane.service.MailService;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequestMapping("/common")
public record CommonController (MailService mailService) {

    @PostMapping("/send-email")
    public ResponseData<String> sendEmail (@RequestParam String recipients,
                                        @RequestParam String subject,
                                      @RequestParam String content,
                                      @RequestParam(required = false) MultipartFile[] files) {
        log.info("Request GET /common/send-email");
        try {
            return new ResponseData<>(HttpStatus.ACCEPTED.value(), mailService.sendMail(recipients, subject, content, files));
        } catch (Exception e) {
            log.error("Sending email was failure, message={}", e.getMessage(), e);
            return new ResponseError<>(HttpStatus.BAD_REQUEST.value(), "Sending email was failure");
        }
    }
}
