package com.linguamastery.api.service;

import com.linguamastery.api.dto.ImportResultResponse;
import com.linguamastery.api.dto.WordRequest;
import com.linguamastery.api.dto.WordResponse;
import com.linguamastery.api.model.User;
import com.linguamastery.api.model.Word;
import com.linguamastery.api.model.WordBook;
import com.linguamastery.api.model.WordLevel;
import com.linguamastery.api.repository.UserRepository;
import com.linguamastery.api.repository.WordBookRepository;
import com.linguamastery.api.repository.WordRepository;
import com.opencsv.CSVReaderBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WordService {

    private final WordRepository wordRepository;
    private final WordBookRepository wordBookRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<WordResponse> getWords(String email, Long bookId) {
        WordBook book = getBookForUser(email, bookId);
        return wordRepository.findByBookIdOrderByCreatedAtDesc(book.getId())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public WordResponse addWord(String email, Long bookId, WordRequest request) {
        WordBook book = getBookForUser(email, bookId);

        Word word = new Word();
        word.setBook(book);
        applyRequest(word, request);

        return toResponse(wordRepository.save(word));
    }

    @Transactional
    public WordResponse updateWord(String email, Long wordId, WordRequest request) {
        Word word = wordRepository.findById(wordId)
                .orElseThrow(() -> new IllegalArgumentException("單字不存在"));

        validateOwnership(email, word);
        applyRequest(word, request);

        return toResponse(wordRepository.save(word));
    }

    // 不標 @Transactional：每行呼叫 wordRepository.save() 各自帶獨立 transaction，
    // 確保部分失敗不會 rollback 已成功的行。
    public ImportResultResponse importWords(String email, Long bookId, MultipartFile file) throws IOException {
        WordBook book = getBookForUser(email, bookId);

        int success = 0;
        int failed = 0;
        List<String> errors = new ArrayList<>();

        try (var reader = new CSVReaderBuilder(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))
                .withSkipLines(1)   // 跳過表頭
                .build()) {

            int lineNum = 1;
            while (true) {
                lineNum++;
                String[] row;
                try {
                    row = reader.readNext();
                    if (row == null) break;
                } catch (Exception e) {
                    failed++;
                    errors.add("第 " + lineNum + " 行：CSV 格式錯誤");
                    break;
                }
                try {
                    if (row.length < 3) {
                        throw new IllegalArgumentException("欄位不足（需要 word, reading, translation）");
                    }
                    String wordStr    = row[0].trim();
                    String reading    = row[1].trim();
                    String translation = row[2].trim();
                    String example    = row.length > 3 ? row[3].trim() : "";
                    String levelStr   = row.length > 4 ? row[4].trim() : "";

                    if (wordStr.isEmpty())    throw new IllegalArgumentException("word 不能為空");
                    if (translation.isEmpty()) throw new IllegalArgumentException("translation 不能為空");

                    WordLevel level = null;
                    if (!levelStr.isEmpty()) {
                        try {
                            level = WordLevel.valueOf(levelStr.toUpperCase());
                        } catch (IllegalArgumentException e) {
                            throw new IllegalArgumentException("不合法的 level 值：" + levelStr);
                        }
                    }

                    Word word = new Word();
                    word.setBook(book);
                    word.setWord(wordStr);
                    word.setReading(reading.isEmpty() ? null : reading);
                    word.setTranslation(translation);
                    word.setExample(example.isEmpty() ? null : example);
                    word.setLevel(level);
                    word.setLanguage(book.getLanguage());
                    wordRepository.save(word);
                    success++;

                } catch (Exception e) {
                    failed++;
                    errors.add("第 " + lineNum + " 行：" + e.getMessage());
                }
            }
        }

        return new ImportResultResponse(success + failed, success, failed, errors);
    }

    @Transactional
    public void deleteWord(String email, Long wordId) {
        Word word = wordRepository.findById(wordId)
                .orElseThrow(() -> new IllegalArgumentException("單字不存在"));

        validateOwnership(email, word);
        wordRepository.delete(word);
    }

    private void applyRequest(Word word, WordRequest request) {
        word.setWord(request.getWord());
        word.setReading(request.getReading());
        word.setTranslation(request.getTranslation());
        word.setExample(request.getExample());
        word.setLevel(request.getLevel());
        word.setLanguage(request.getLanguage());
    }

    private WordBook getBookForUser(String email, Long bookId) {
        User user = getUser(email);
        WordBook book = wordBookRepository.findById(bookId)
                .orElseThrow(() -> new IllegalArgumentException("單字本不存在"));

        if (!book.getUser().getId().equals(user.getId())) {
            throw new SecurityException("無權限操作此資源");
        }
        return book;
    }

    private void validateOwnership(String email, Word word) {
        User user = getUser(email);
        if (!word.getBook().getUser().getId().equals(user.getId())) {
            throw new SecurityException("無權限操作此資源");
        }
    }

    private User getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("使用者不存在"));
    }

    private WordResponse toResponse(Word word) {
        WordResponse response = new WordResponse();
        response.setId(word.getId());
        response.setWord(word.getWord());
        response.setReading(word.getReading());
        response.setTranslation(word.getTranslation());
        response.setExample(word.getExample());
        response.setLevel(word.getLevel());
        response.setLanguage(word.getLanguage());
        response.setCreatedAt(word.getCreatedAt());
        return response;
    }
}
