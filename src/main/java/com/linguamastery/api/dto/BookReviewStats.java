package com.linguamastery.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class BookReviewStats {
    private Long bookId;
    private String bookName;
    private long dueCount;   // 已學過、今日需複習
    private long newCount;   // 尚未學過的新單字（上限 5）
}
