package com.linguamastery.api.service;

import com.linguamastery.api.dto.*;
import com.linguamastery.api.model.User;
import com.linguamastery.api.repository.UserRepository;
import com.linguamastery.api.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;
    private final EmailService emailService;

    @Transactional
    public MessageResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("此 Email 已被使用");
        }

        String token = UUID.randomUUID().toString();

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEmailVerified(false);
        user.setVerificationToken(token);
        user.setVerificationTokenExpiry(LocalDateTime.now().plusHours(24));
        userRepository.save(user);

        emailService.sendVerificationEmail(user.getEmail(), token);

        return new MessageResponse("驗證信已寄出，請查收 Email 完成驗證");
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("用戶不存在"));

        if (!user.isEmailVerified()) {
            throw new IllegalStateException("EMAIL_NOT_VERIFIED");
        }

        String token = jwtTokenProvider.generateToken(user.getEmail());
        return new AuthResponse(token, user.getEmail());
    }

    public MessageResponse verifyEmail(String token) {
        User user = userRepository.findByVerificationToken(token)
                .orElseThrow(() -> new IllegalArgumentException("無效的驗證連結"));

        if (user.getVerificationTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("驗證連結已過期，請重新申請");
        }

        user.setEmailVerified(true);
        user.setVerificationToken(null);
        user.setVerificationTokenExpiry(null);
        userRepository.save(user);

        return new MessageResponse("Email 驗證成功！請登入");
    }

    public MessageResponse resendVerification(ForgotPasswordRequest request) {
        userRepository.findByEmail(request.getEmail())
                .filter(u -> !u.isEmailVerified())
                .ifPresent(user -> {
                    String token = UUID.randomUUID().toString();
                    user.setVerificationToken(token);
                    user.setVerificationTokenExpiry(LocalDateTime.now().plusHours(24));
                    userRepository.save(user);
                    emailService.sendVerificationEmail(user.getEmail(), token);
                });
        // 無論 Email 是否存在或已驗證，都回相同訊息（避免帳號列舉攻擊）
        return new MessageResponse("驗證信已重新寄出，請查收信箱");
    }

    public MessageResponse forgotPassword(ForgotPasswordRequest request) {
        userRepository.findByEmail(request.getEmail()).ifPresent(user -> {
            String token = UUID.randomUUID().toString();
            user.setResetToken(token);
            user.setResetTokenExpiry(LocalDateTime.now().plusMinutes(15));
            userRepository.save(user);
            emailService.sendPasswordResetEmail(user.getEmail(), token);
        });
        // 無論 Email 是否存在，都回相同訊息（避免帳號列舉攻擊）
        return new MessageResponse("若此 Email 已註冊，重設密碼連結將寄出");
    }

    public MessageResponse resetPassword(ResetPasswordRequest request) {
        User user = userRepository.findByResetToken(request.getToken())
                .orElseThrow(() -> new IllegalArgumentException("無效的重設連結"));

        if (user.getResetTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("重設連結已過期（15 分鐘），請重新申請");
        }

        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setResetToken(null);
        user.setResetTokenExpiry(null);
        userRepository.save(user);

        return new MessageResponse("密碼重設成功，請重新登入");
    }
}
