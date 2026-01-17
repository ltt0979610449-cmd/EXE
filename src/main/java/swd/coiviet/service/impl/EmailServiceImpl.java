package swd.coiviet.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import swd.coiviet.service.EmailService;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.util.List;

@Service
public class EmailServiceImpl implements EmailService {
    private static final Logger logger = LoggerFactory.getLogger(EmailServiceImpl.class);
    
    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Override
    public void sendEmail(String to, String subject, String body) {
        if (mailSender == null) {
            logger.warn("JavaMailSender not configured, skipping email to: {}", to);
            return;
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body, true); // HTML content
            
            mailSender.send(message);
            logger.info("Email sent successfully to: {}", to);
        } catch (MessagingException e) {
            logger.error("Failed to send email to {}: {}", to, e.getMessage(), e);
        } catch (Exception e) {
            logger.error("Unexpected error sending email to {}: {}", to, e.getMessage(), e);
        }
    }

    @Override
    public void sendBookingConfirmation(String to, String bookingCode, String tourTitle, String tourDate) {
        String subject = "Xác nhận đặt tour - " + bookingCode;
        String body = String.format("""
            <html>
            <body style="font-family: Arial, sans-serif;">
                <h2 style="color: #2c3e50;">Xác nhận đặt tour thành công!</h2>
                <p>Xin chào,</p>
                <p>Cảm ơn bạn đã đặt tour tại Cội Việt. Thông tin đặt tour của bạn:</p>
                <ul>
                    <li><strong>Mã đặt tour:</strong> %s</li>
                    <li><strong>Tên tour:</strong> %s</li>
                    <li><strong>Ngày khởi hành:</strong> %s</li>
                </ul>
                <p>Vui lòng thanh toán để hoàn tất đặt tour. Chúng tôi sẽ gửi thông báo khi nhận được thanh toán.</p>
                <p>Trân trọng,<br>Đội ngũ Cội Việt</p>
            </body>
            </html>
            """, bookingCode, tourTitle, tourDate);
        sendEmail(to, subject, body);
    }

    @Override
    public void sendBookingCancellation(String to, String bookingCode, String reason, java.math.BigDecimal refundAmount) {
        String subject = "Thông báo hủy tour - " + bookingCode;
        String body = String.format("""
            <html>
            <body style="font-family: Arial, sans-serif;">
                <h2 style="color: #e74c3c;">Thông báo hủy tour</h2>
                <p>Xin chào,</p>
                <p>Tour của bạn đã được hủy với thông tin sau:</p>
                <ul>
                    <li><strong>Mã đặt tour:</strong> %s</li>
                    <li><strong>Lý do:</strong> %s</li>
                    <li><strong>Số tiền hoàn lại:</strong> %s VNĐ</li>
                </ul>
                <p>Tiền hoàn lại sẽ được chuyển về tài khoản của bạn trong vòng 5-7 ngày làm việc.</p>
                <p>Trân trọng,<br>Đội ngũ Cội Việt</p>
            </body>
            </html>
            """, bookingCode, reason != null ? reason : "Không có", 
                refundAmount != null ? refundAmount.toString() : "0");
        sendEmail(to, subject, body);
    }

    @Override
    public void sendTourLowBookingAlert(String to, String tourTitle, String tourDate, String voucherCode, Integer discountPercent) {
        String subject = "Khuyến mãi đặc biệt cho tour: " + tourTitle;
        String body = String.format("""
            <html>
            <body style="font-family: Arial, sans-serif;">
                <h2 style="color: #27ae60;">Khuyến mãi đặc biệt!</h2>
                <p>Xin chào,</p>
                <p>Tour <strong>%s</strong> vào ngày <strong>%s</strong> đang có khuyến mãi đặc biệt!</p>
                <p style="font-size: 18px; color: #e74c3c;"><strong>Giảm giá %d%%</strong></p>
                <p><strong>Mã voucher:</strong> <code style="background: #f0f0f0; padding: 5px 10px; border-radius: 3px;">%s</code></p>
                <p>Hãy nhanh tay đặt tour để nhận ưu đãi này!</p>
                <p>Trân trọng,<br>Đội ngũ Cội Việt</p>
            </body>
            </html>
            """, tourTitle, tourDate, discountPercent, voucherCode);
        sendEmail(to, subject, body);
    }

    @Override
    public void sendTourCancellationNotice(String to, String tourTitle, String tourDate, String reason) {
        String subject = "Thông báo hủy tour: " + tourTitle;
        String body = String.format("""
            <html>
            <body style="font-family: Arial, sans-serif;">
                <h2 style="color: #e74c3c;">Thông báo hủy tour</h2>
                <p>Xin chào,</p>
                <p>Chúng tôi rất tiếc phải thông báo rằng tour <strong>%s</strong> vào ngày <strong>%s</strong> đã bị hủy.</p>
                <p><strong>Lý do:</strong> %s</p>
                <p>Chúng tôi sẽ tự động hoàn lại toàn bộ số tiền đã thanh toán cho bạn trong vòng 5-7 ngày làm việc.</p>
                <p>Xin lỗi vì sự bất tiện này. Chúng tôi rất mong được phục vụ bạn trong các tour khác.</p>
                <p>Trân trọng,<br>Đội ngũ Cội Việt</p>
            </body>
            </html>
            """, tourTitle, tourDate, reason != null ? reason : "Không đủ số lượng người đăng ký");
        sendEmail(to, subject, body);
    }

    @Override
    public void sendTourSurchargeNotice(String to, String tourTitle, String tourDate, java.math.BigDecimal surchargeAmount) {
        String subject = "Thông báo phụ thu tour: " + tourTitle;
        String body = String.format("""
            <html>
            <body style="font-family: Arial, sans-serif;">
                <h2 style="color: #f39c12;">Thông báo phụ thu</h2>
                <p>Xin chào,</p>
                <p>Tour <strong>%s</strong> vào ngày <strong>%s</strong> có số lượng đăng ký thấp.</p>
                <p>Để đảm bảo chất lượng tour, chúng tôi cần áp dụng phụ thu: <strong>%s VNĐ</strong></p>
                <p>Bạn có thể chọn:</p>
                <ul>
                    <li>Thanh toán phụ thu để tiếp tục tour</li>
                    <li>Hủy tour và nhận hoàn tiền theo chính sách hủy tour</li>
                </ul>
                <p>Vui lòng phản hồi trong vòng 24 giờ.</p>
                <p>Trân trọng,<br>Đội ngũ Cội Việt</p>
            </body>
            </html>
            """, tourTitle, tourDate, surchargeAmount != null ? surchargeAmount.toString() : "0");
        sendEmail(to, subject, body);
    }

    @Override
    public void sendAlternativeTourSuggestion(String to, String originalTourTitle, List<String> alternativeTours) {
        String subject = "Đề xuất tour thay thế";
        StringBuilder toursList = new StringBuilder();
        for (int i = 0; i < alternativeTours.size(); i++) {
            toursList.append(String.format("<li>%d. %s</li>", i + 1, alternativeTours.get(i)));
        }
        
        String body = String.format("""
            <html>
            <body style="font-family: Arial, sans-serif;">
                <h2 style="color: #3498db;">Đề xuất tour thay thế</h2>
                <p>Xin chào,</p>
                <p>Tour <strong>%s</strong> của bạn có thể không đủ số lượng. Chúng tôi xin đề xuất các tour thay thế:</p>
                <ul>
                    %s
                </ul>
                <p>Vui lòng phản hồi để chúng tôi có thể hỗ trợ bạn chuyển đổi tour.</p>
                <p>Trân trọng,<br>Đội ngũ Cội Việt</p>
            </body>
            </html>
            """, originalTourTitle, toursList.toString());
        sendEmail(to, subject, body);
    }

    @Override
    public void sendPasswordResetOtp(String to, String otp) {
        String subject = "Mã OTP đặt lại mật khẩu - Cội Việt";
        String body = String.format("""
            <html>
            <body style="font-family: Arial, sans-serif;">
                <h2 style="color: #2c3e50;">Đặt lại mật khẩu</h2>
                <p>Xin chào,</p>
                <p>Bạn đã yêu cầu đặt lại mật khẩu cho tài khoản của bạn.</p>
                <p>Mã OTP của bạn là:</p>
                <div style="background-color: #f0f0f0; padding: 20px; text-align: center; margin: 20px 0; border-radius: 5px;">
                    <h1 style="color: #3498db; font-size: 32px; letter-spacing: 5px; margin: 0;">%s</h1>
                </div>
                <p><strong>Lưu ý:</strong></p>
                <ul>
                    <li>Mã OTP này sẽ hết hạn sau <strong>10 phút</strong></li>
                    <li>Mã OTP chỉ có hiệu lực <strong>1 lần</strong></li>
                    <li>Bạn có tối đa <strong>5 lần</strong> nhập sai OTP</li>
                    <li>Không chia sẻ mã OTP này với bất kỳ ai</li>
                </ul>
                <p>Nếu bạn không yêu cầu đặt lại mật khẩu, vui lòng bỏ qua email này và đảm bảo tài khoản của bạn được bảo mật.</p>
                <p>Trân trọng,<br>Đội ngũ Cội Việt</p>
            </body>
            </html>
            """, otp);
        sendEmail(to, subject, body);
    }
}
