package com.linguamastery.api.repository;

import com.linguamastery.api.model.WordBook;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WordBookRepository extends JpaRepository<WordBook, Long> {
    List<WordBook> findByUserIdOrderByCreatedAtDesc(Long userId);
}
