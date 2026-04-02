// top/yyf/psych_support/entity/AssessmentQuestion.java
package top.yyf.psych_support.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("assessment_question")
public class AssessmentQuestion {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long assessmentId;
    private String content;
    private Integer seq;
    private String scoreType; // single_choice, likert_4, etc.

    @TableLogic
    private Boolean deleted;

    private LocalDateTime createdAt;
}