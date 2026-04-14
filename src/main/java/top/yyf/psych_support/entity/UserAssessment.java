// top/yyf/psych_support/entity/UserAssessment.java
package top.yyf.psych_support.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
@TableName(value = "user_assessment", autoResultMap = true)  // ✅ 添加 autoResultMap = true
public class UserAssessment {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;
    private Long assessmentId;
    private Integer totalScore;
    private String resultLevel;

    // ✅ 确保这个注解正确
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<AnswerItem> rawData;

    private LocalDateTime completedAt;
    private LocalDateTime createdAt;

    @Data
    public static class AnswerItem {
        private Long questionId;
        private Long optionId;
    }
}