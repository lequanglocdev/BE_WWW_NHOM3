package fit.iuh.se.services;

import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    @Autowired
    private JavaMailSender mailSender;
    public void sendVerify(String email, String token) {
        try {
            String link = "http://localhost:8081/api/auth/verify?token=" + token;

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(email);
            helper.setSubject("Xác thực tài khoản - MyApp");

            String html = """
            <div style="font-family: Arial; max-width:600px; margin:auto;">
                <h2 style="color:#2c3e50;">Chào bạn 👋</h2>

                <p>Cảm ơn bạn đã đăng ký tài khoản tại <b>MyApp</b>.</p>

                <p>Vui lòng nhấn nút bên dưới để xác thực email:</p>

                <div style="text-align:center; margin:30px;">
                    <a href="%s"
                       style="
                        background:#28a745;
                        color:white;
                        padding:12px 20px;
                        text-decoration:none;
                        border-radius:5px;
                        font-weight:bold;">
                        Xác thực tài khoản
                    </a>
                </div>

                <p>Hoặc copy link:</p>
                <p>%s</p>

                <hr>
                <p style="font-size:12px;color:gray;">
                    Link có hiệu lực trong 24 giờ.
                </p>
            </div>
        """.formatted(link, link);

            helper.setText(html, true);
            mailSender.send(message);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void sendReset(String email, String token) {
        try {
            String link = "http://localhost:3000/reset-password?token=" + token;

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(email);
            helper.setSubject("Đặt lại mật khẩu - MyApp");

            String html = """
            <div style="font-family: Arial; max-width:600px; margin:auto;">
                <h2>🔐 Yêu cầu đặt lại mật khẩu</h2>

                <p>Chúng tôi nhận được yêu cầu đặt lại mật khẩu của bạn.</p>

                <div style="text-align:center; margin:30px;">
                    <a href="%s"
                       style="
                        background:#dc3545;
                        color:white;
                        padding:12px 20px;
                        text-decoration:none;
                        border-radius:5px;">
                        Đặt lại mật khẩu
                    </a>
                </div>

                <p>Nếu bạn không yêu cầu, hãy bỏ qua email này.</p>

                <hr>
                <p style="font-size:12px;color:gray;">
                    Link hết hạn sau 15 phút.
                </p>
            </div>
        """.formatted(link);

            helper.setText(html, true);
            mailSender.send(message);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
