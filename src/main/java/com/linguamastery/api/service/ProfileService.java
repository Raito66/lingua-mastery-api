package com.linguamastery.api.service;

import com.linguamastery.api.dto.ChangePasswordRequest;
import com.linguamastery.api.dto.MessageResponse;
import com.linguamastery.api.dto.ProfileRequest;
import com.linguamastery.api.dto.ProfileResponse;
import com.linguamastery.api.model.User;
import com.linguamastery.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public ProfileResponse getProfile(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("用戶不存在"));
        return new ProfileResponse(user.getEmail(), user.getDisplayName());
    }

    @Transactional
    public ProfileResponse updateProfile(String email, ProfileRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("用戶不存在"));
        user.setDisplayName(request.getDisplayName().trim());
        userRepository.save(user);
        return new ProfileResponse(user.getEmail(), user.getDisplayName());
    }

    @Transactional
    public MessageResponse changePassword(String email, ChangePasswordRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("用戶不存在"));
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new IllegalArgumentException("目前密碼不正確");
        }
        if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
            throw new IllegalArgumentException("新密碼不能與目前密碼相同");
        }
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        return new MessageResponse("密碼已更新");
    }
}
