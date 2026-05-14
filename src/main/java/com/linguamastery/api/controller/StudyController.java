package com.linguamastery.api.controller;

import com.linguamastery.api.dto.StudyResultRequest;
import com.linguamastery.api.dto.WordResponse;
import com.linguamastery.api.service.StudyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/study")
@RequiredArgsConstructor
public class StudyController {

    private final StudyService studyService;

    @GetMapping("/{bookId}")
    public ResponseEntity<List<WordResponse>> getStudyWords(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long bookId) {
        return ResponseEntity.ok(studyService.getStudyWords(userDetails.getUsername(), bookId));
    }

    @PostMapping("/result")
    public ResponseEntity<Void> submitResult(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody StudyResultRequest request) {
        studyService.recordResult(userDetails.getUsername(), request);
        return ResponseEntity.ok().build();
    }
}
