package uz.drivesmart.service.impl;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import uz.drivesmart.exception.BusinessException;

/**
 * Email yuborish xizmati
 */
@Service
@Slf4j
public class EmailService {

    @Value("${app.mail.username}")
    private String fromEmail;

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    /**
     * Oddiy text email yuborish
     */
    public void sendEmail(String to, String subject, String text) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(text, false);

            mailSender.send(message);
            log.info("Email muvaffaqiyatli yuborildi: {}", to);

        } catch (MessagingException e) {
            log.error("Email yuborishda xatolik: {}", e.getMessage());
            throw new BusinessException("Email yuborishda xatolik yuz berdi");
        }
    }

    /**
     * HTML email yuborish
     */
    public void sendHtmlEmail(String to, String subject, String htmlContent) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true); // true = HTML

            mailSender.send(message);
            log.info("HTML Email muvaffaqiyatli yuborildi: {}", to);

        } catch (MessagingException e) {
            log.error("HTML Email yuborishda xatolik: {}", e.getMessage());
            throw new BusinessException("Email yuborishda xatolik yuz berdi");
        }
    }

    /**
     * Tasdiqlash kodi uchun email shablon
     */
    public void sendVerificationCode(String to, String code) {
        String subject = "DriveSmart - Tasdiqlash Kodi";
        String htmlContent = String.format("""
            <html>
            <body style="font-family: Arial, sans-serif; line-height: 1.6; color: #333;">
                <div style="max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #ddd; border-radius: 5px;">
                    <h2 style="color: #4CAF50;">DriveSmart</h2>
                    <p>Assalomu alaykum!</p>
                    <p>Sizning tasdiqlash kodingiz:</p>
                    <div style="background-color: #f4f4f4; padding: 15px; text-align: center; font-size: 24px; font-weight: bold; letter-spacing: 5px; border-radius: 5px; margin: 20px 0;">
                        %s
                    </div>
                    <p>Bu kod 5 daqiqa davomida amal qiladi.</p>
                    <p style="color: #888; font-size: 12px; margin-top: 30px;">
                        Agar siz bu so'rovni yubormagan bo'lsangiz, iltimos bu xabarni e'tiborsiz qoldiring.
                    </p>
                </div>
            </body>
            </html>
            """, code);

        sendHtmlEmail(to, subject, htmlContent);
    }
}