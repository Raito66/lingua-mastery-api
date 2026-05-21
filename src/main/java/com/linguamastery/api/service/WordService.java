package com.linguamastery.api.service;

import com.linguamastery.api.dto.ImportResultResponse;
import com.linguamastery.api.dto.WordRequest;
import com.linguamastery.api.dto.WordResponse;
import com.linguamastery.api.model.User;
import com.linguamastery.api.model.Word;
import com.linguamastery.api.model.WordBook;
import com.linguamastery.api.model.WordLevel;
import com.linguamastery.api.model.UserWordStatus;
import com.linguamastery.api.repository.UserRepository;
import com.linguamastery.api.repository.UserWordStatusRepository;
import com.linguamastery.api.repository.WordBookRepository;
import com.linguamastery.api.repository.WordRepository;
import com.opencsv.CSVReaderBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WordService {

    private final WordRepository wordRepository;
    private final WordBookRepository wordBookRepository;
    private final UserRepository userRepository;
    private final UserWordStatusRepository statusRepository;

    @Transactional(readOnly = true)
    public List<WordResponse> getWords(String email, Long bookId) {
        User user = getUser(email);
        WordBook book = wordBookRepository.findById(bookId)
                .orElseThrow(() -> new IllegalArgumentException("單字本不存在"));
        if (!book.getUser().getId().equals(user.getId())) {
            throw new SecurityException("無權限操作此資源");
        }

        // 批次取得熟練度，避免 N+1
        Map<Long, Integer> levelMap = statusRepository
                .findByUserIdAndBookId(user.getId(), bookId)
                .stream()
                .collect(Collectors.toMap(s -> s.getWord().getId(), UserWordStatus::getLevel));

        return wordRepository.findByBookIdOrderByCreatedAtDesc(bookId)
                .stream()
                .map(word -> toResponse(word, levelMap.getOrDefault(word.getId(), 0)))
                .toList();
    }

    @Transactional
    public WordResponse addWord(String email, Long bookId, WordRequest request) {
        WordBook book = getBookForUser(email, bookId);

        if (wordRepository.existsByBookIdAndWord(bookId, request.getWord().trim())) {
            throw new IllegalArgumentException("「" + request.getWord().trim() + "」已存在於此單字本");
        }

        Word word = new Word();
        word.setBook(book);
        applyRequest(word, request);

        return toResponse(wordRepository.save(word), 0);
    }

    @Transactional
    public WordResponse updateWord(String email, Long wordId, WordRequest request) {
        Word word = wordRepository.findById(wordId)
                .orElseThrow(() -> new IllegalArgumentException("單字不存在"));

        validateOwnership(email, word);
        applyRequest(word, request);

        User user = getUser(email);
        int level = statusRepository.findByUserIdAndWordId(user.getId(), wordId)
                .map(UserWordStatus::getLevel).orElse(0);
        return toResponse(wordRepository.save(word), level);
    }

    private static final int MAX_IMPORT_ROWS = 500;

    /**
     * 偵測 CSV 編碼並計算 BOM offset，回傳 [charset, bomByteCount]。
     * 優先檢查 UTF-8 BOM（EF BB BF），再嘗試嚴格 UTF-8 解碼，失敗則 fallback 至 Windows-31J（Shift-JIS）。
     */
    private static Object[] detectCharsetAndBom(byte[] bytes) {
        if (bytes.length >= 3
                && bytes[0] == (byte) 0xEF
                && bytes[1] == (byte) 0xBB
                && bytes[2] == (byte) 0xBF) {
            return new Object[]{ StandardCharsets.UTF_8, 3 };
        }
        try {
            CharsetDecoder decoder = StandardCharsets.UTF_8.newDecoder()
                    .onMalformedInput(CodingErrorAction.REPORT)
                    .onUnmappableCharacter(CodingErrorAction.REPORT);
            decoder.decode(ByteBuffer.wrap(bytes));
            return new Object[]{ StandardCharsets.UTF_8, 0 };
        } catch (Exception e) {
            return new Object[]{ Charset.forName("Windows-31J"), 0 }; // Shift-JIS（日文 Windows Excel 預設）
        }
    }

    // 不標 @Transactional：每行呼叫 wordRepository.save() 各自帶獨立 transaction，
    // 確保部分失敗不會 rollback 已成功的行。
    public ImportResultResponse importWords(String email, Long bookId, MultipartFile file) throws IOException {
        WordBook book = getBookForUser(email, bookId);

        int success = 0;
        int failed = 0;
        List<String> errors = new ArrayList<>();

        byte[] bytes = file.getBytes();
        Object[] charsetAndBom = detectCharsetAndBom(bytes);
        Charset charset = (Charset) charsetAndBom[0];
        int offset = ((Number) charsetAndBom[1]).intValue();

        try (var reader = new CSVReaderBuilder(
                new InputStreamReader(
                        new ByteArrayInputStream(bytes, offset, bytes.length - offset), charset))
                .withSkipLines(1)   // 跳過表頭
                .build()) {

            int lineNum = 1;       // 第 1 行為表頭（已跳過），從第 2 行開始計
            int rowsRead = 0;
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
                rowsRead++;
                if (rowsRead > MAX_IMPORT_ROWS) {
                    errors.add("已達最大匯入上限（" + MAX_IMPORT_ROWS + " 筆），後續資料已略過");
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
                    if (wordRepository.existsByBookIdAndWord(book.getId(), wordStr)) {
                        throw new IllegalArgumentException("單字已存在：" + wordStr);
                    }

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

    @Transactional
    public void deleteWords(String email, List<Long> wordIds) {
        if (wordIds == null || wordIds.isEmpty()) {
            throw new IllegalArgumentException("請至少選擇一個單字");
        }
        List<Word> words = wordRepository.findAllById(wordIds);
        words.forEach(word -> validateOwnership(email, word));
        wordRepository.deleteAll(words);
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

    private WordResponse toResponse(Word word, int proficiencyLevel) {
        WordResponse response = new WordResponse();
        response.setId(word.getId());
        response.setWord(word.getWord());
        response.setReading(word.getReading());
        response.setTranslation(word.getTranslation());
        response.setExample(word.getExample());
        response.setLevel(word.getLevel());
        response.setLanguage(word.getLanguage());
        response.setCreatedAt(word.getCreatedAt());
        response.setProficiencyLevel(proficiencyLevel);
        return response;
    }
}
