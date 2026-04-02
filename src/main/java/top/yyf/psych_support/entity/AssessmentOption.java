// top/yyf/psych_support/entity/AssessmentOption.java
package top.yyf.psych_support.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("assessment_option")
public class AssessmentOption {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long questionId;
    private String content;
    private Integer score;
    private Integer seq;

    @TableLogic
    private Boolean deleted;

    private LocalDateTime createdAt;
}