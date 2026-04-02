// top/yyf/psych_support/entity/UserAssessment.java
package top.yyf.psych_support.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
@TableName("user_assessment")
public class UserAssessment {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;
    private Long assessmentId;
    private Integer totalScore;
    private String resultLevel;

    @TableField(typeHandler = com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler.class)
    private List<AnswerItem> rawData; // JSON → List

    private LocalDateTime completedAt;
    private LocalDateTime createdAt;

    // 答题项内部类
    @Data
    public static class AnswerItem {
        private Long questionId;
        private Long optionId;
    }
}