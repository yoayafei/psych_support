// top/yyf/psych_support/entity/Assessment.java
package top.yyf.psych_support.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("assessment")
public class Assessment {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;
    private String description;
    private String instructions;
    private String category;
    private Boolean isActive; // 对应 is_active

    @TableField(typeHandler = com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler.class)
    private ScoringRules scoringRules; // JSON 字段 → 自定义对象

    @TableLogic
    private Boolean deleted;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // JSON 内部类（可选：也可单独建类）
    @Data
    public static class ScoringRules {
        private Range[] ranges;

        @Data
        public static class Range {
            private Integer min;
            private Integer max;
            private String level;
            private String advice;
        }
    }
}