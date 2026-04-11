package top.yyf.psych_support.model.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MoodDiaryVO {
    private Long id;
    private Long userId;
    private String nickname; // 昵称
    private String content;
    private String moodTag;
    private Byte moodLevel; // 情绪强度 1-5
    private String imageUrl;
    private Byte isPublic; // 0=私密,1=待审核,2=已公开
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateAt;
    private String formattedDate; // 格式化后的日期
}