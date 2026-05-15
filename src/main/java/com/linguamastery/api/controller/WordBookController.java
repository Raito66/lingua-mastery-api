package com.linguamastery.api.controller;

import com.linguamastery.api.dto.WordBookRequest;
import com.linguamastery.api.dto.WordBookResponse;
import com.linguamastery.api.service.WordBookService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
public class WordBookController {

    private final WordBookService wordBookService;

    @GetMapping
    public ResponseEntity<List<WordBookResponse>> getBooks(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(wordBookService.getBooks(userDetails.getUsername()));
    }

    @PostMapping
    public ResponseEntity<WordBookResponse> createBook(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody WordBookRequest request) {
        return ResponseEntity.ok(wordBookService.createBook(userDetails.getUsername(), request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<WordBookResponse> updateBook(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id,
            @Valid @RequestBody WordBookRequest request) {
        return ResponseEntity.ok(wordBookService.updateBook(userDetails.getUsername(), id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBook(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        wordBookService.deleteBook(userDetails.getUsername(), id);
        return ResponseEntity.noContent().build();
    }
}
