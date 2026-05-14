package com.linguamastery.api.controller;

import com.linguamastery.api.dto.WordRequest;
import com.linguamastery.api.dto.WordResponse;
import com.linguamastery.api.service.WordService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class WordController {

    private final WordService wordService;

    @GetMapping("/api/books/{bookId}/words")
    public ResponseEntity<List<WordResponse>> getWords(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long bookId) {
        return ResponseEntity.ok(wordService.getWords(userDetails.getUsername(), bookId));
    }

    @PostMapping("/api/books/{bookId}/words")
    public ResponseEntity<WordResponse> addWord(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long bookId,
            @Valid @RequestBody WordRequest request) {
        return ResponseEntity.ok(wordService.addWord(userDetails.getUsername(), bookId, request));
    }

    @PutMapping("/api/words/{id}")
    public ResponseEntity<WordResponse> updateWord(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id,
            @Valid @RequestBody WordRequest request) {
        return ResponseEntity.ok(wordService.updateWord(userDetails.getUsername(), id, request));
    }

    @DeleteMapping("/api/words/{id}")
    public ResponseEntity<Void> deleteWord(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        wordService.deleteWord(userDetails.getUsername(), id);
        return ResponseEntity.noContent().build();
    }
}
