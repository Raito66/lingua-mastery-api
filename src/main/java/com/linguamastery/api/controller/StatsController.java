package com.linguamastery.api.controller;

import com.linguamastery.api.dto.StatsResponse;
import com.linguamastery.api.dto.StreakResponse;
import com.linguamastery.api.service.StatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/stats")
@RequiredArgsConstructor
public class StatsController {

    private final StatsService statsService;

    @GetMapping
    public ResponseEntity<StatsResponse> getStats(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(statsService.getStats(userDetails.getUsername()));
    }

    @GetMapping("/streak")
    public ResponseEntity<StreakResponse> getStreak(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(statsService.getStreak(userDetails.getUsername()));
    }
}
