package com.linguamastery.api.controller;

import com.linguamastery.api.dto.BookReviewStats;
import com.linguamastery.api.dto.ReviewResultRequest;
import com.linguamastery.api.dto.WordResponse;
import com.linguamastery.api.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/review")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    /** 取得所有單字本的今日複習統計（用於首頁顯示徽章） */
    @GetMapping("/stats")
    public ResponseEntity<List<BookReviewStats>> getStats(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(reviewService.getReviewStats(userDetails.getUsername()));
    }

    /** 取得指定單字本的今日複習單字（到期 + 新單字） */
    @GetMapping("/{bookId}")
    public ResponseEntity<List<WordResponse>> getReviewWords(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long bookId) {
        return ResponseEntity.ok(reviewService.getReviewWords(userDetails.getUsername(), bookId));
    }

    /** 提交複習結果，依 SM-2 更新下次複習排程 */
    @PostMapping("/result")
    public ResponseEntity<Void> submitReview(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody ReviewResultRequest request) {
        reviewService.submitReview(
                userDetails.getUsername(), request.getWordId(), request.getCorrect());
        return ResponseEntity.ok().build();
    }
}
