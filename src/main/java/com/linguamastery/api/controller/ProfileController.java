package com.linguamastery.api.controller;

import com.linguamastery.api.dto.ChangePasswordRequest;
import com.linguamastery.api.dto.MessageResponse;
import com.linguamastery.api.dto.ProfileRequest;
import com.linguamastery.api.dto.ProfileResponse;
import com.linguamastery.api.service.ProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

    @GetMapping
    public ResponseEntity<ProfileResponse> getProfile(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(profileService.getProfile(userDetails.getUsername()));
    }

    @PutMapping
    public ResponseEntity<ProfileResponse> updateProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody ProfileRequest request) {
        return ResponseEntity.ok(profileService.updateProfile(userDetails.getUsername(), request));
    }

    @PutMapping("/password")
    public ResponseEntity<MessageResponse> changePassword(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody ChangePasswordRequest request) {
        return ResponseEntity.ok(profileService.changePassword(userDetails.getUsername(), request));
    }
}
