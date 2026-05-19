package com.linguamastery.api.controller;

import com.linguamastery.api.dto.QuizQuestion;
import com.linguamastery.api.service.QuizService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/quiz")
@RequiredArgsConstructor
public class QuizController {

    private final QuizService quizService;

    @GetMapping("/{bookId}")
    public ResponseEntity<List<QuizQuestion>> getQuizQuestions(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long bookId) {
        return ResponseEntity.ok(quizService.getQuizQuestions(userDetails.getUsername(), bookId));
    }
}
