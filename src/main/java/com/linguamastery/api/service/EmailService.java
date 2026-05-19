package com.linguamastery.api.service;

import com.resend.Resend;
import com.resend.services.emails.model.CreateEmailOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final Resend resend;

    @Value("${app.mail.from}")
    private String from;

    @Value("${app.url}")
    private String appUrl;

    public EmailService(@Value("${resend.api-key}") String apiKey) {
        this.resend = new Resend(apiKey);
    }

    public void sendVerificationEmail(String to, String token) {
        String link = appUrl + "/verify-email?token=" + token;
        String html = buildEmail(
                "驗證您的 Email",
                "感謝您註冊 LinguaMastery！請點擊下方按鈕完成 Email 驗證，即可開始學習。",
                link,
                "立即驗證",
                "此連結將在 24 小時後失效"
        );
        sendHtmlEmail(to, "【LinguaMastery】請驗證您的 Email", html);
    }

    public void sendPasswordResetEmail(String to, String token) {
        String link = appUrl + "/reset-password?token=" + token;
        String html = buildEmail(
                "重設您的密碼",
                "我們收到了您的密碼重設請求。請點擊下方按鈕設定新密碼。若您未提出此請求，請忽略此信。",
                link,
                "重設密碼",
                "此連結將在 15 分鐘後失效"
        );
        sendHtmlEmail(to, "【LinguaMastery】重設密碼", html);
    }

    private void sendHtmlEmail(String to, String subject, String html) {
        try {
            CreateEmailOptions params = CreateEmailOptions.builder()
                    .from(from)
                    .to(to)
                    .subject(subject)
                    .html(html)
                    .build();
            resend.emails().send(params);
        } catch (Exception e) {
            throw new RuntimeException("郵件發送失敗：" + e.getMessage(), e);
        }
    }

    private String buildEmail(String title, String body, String link, String buttonText, String expiry) {
        return """
                <!DOCTYPE html>
                <html lang="zh-TW">
                <head><meta charset="UTF-8"><meta name="viewport" content="width=device-width,initial-scale=1"></head>
                <body style="margin:0;padding:20px;background:#f0f2f5;font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',sans-serif;">
                  <div style="max-width:480px;margin:0 auto;background:#ffffff;border-radius:16px;overflow:hidden;box-shadow:0 4px 16px rgba(0,0,0,0.08);">
                    <div style="background:linear-gradient(135deg,#7C6AFA,#9C8CFF);padding:32px;text-align:center;">
                      <p style="font-size:40px;margin:0;">🌐</p>
                      <h1 style="color:#ffffff;margin:8px 0 0;font-size:22px;font-weight:700;letter-spacing:0.5px;">LinguaMastery</h1>
                      <p style="color:rgba(255,255,255,0.7);margin:4px 0 0;font-size:13px;">高手之路，從單字開始</p>
                    </div>
                    <div style="padding:32px;">
                      <h2 style="color:#1a1a2e;font-size:18px;margin:0 0 16px;">%s</h2>
                      <p style="color:#555;line-height:1.7;margin:0 0 24px;font-size:14px;">%s</p>
                      <div style="text-align:center;margin:24px 0;">
                        <a href="%s" style="display:inline-block;padding:14px 36px;background:#7C6AFA;color:#ffffff;text-decoration:none;border-radius:10px;font-weight:600;font-size:15px;">%s</a>
                      </div>
                      <p style="color:#aaa;font-size:12px;text-align:center;margin:16px 0 0;">%s</p>
                    </div>
                    <div style="background:#f8f9fa;padding:16px;text-align:center;">
                      <p style="color:#bbb;font-size:12px;margin:0;">© 2025 LinguaMastery</p>
                    </div>
                  </div>
                </body>
                </html>
                """.formatted(title, body, link, buttonText, expiry);
    }
}
