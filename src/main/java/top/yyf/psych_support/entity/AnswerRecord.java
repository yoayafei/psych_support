package top.yyf.psych_support.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@TableName("answer_record")
public class AnswerRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("user_id")
    private Long userId;

    @TableField("assessment_id")
    private Long assessmentId;

    @TableField("total_score")
    private Integer totalScore;

    /**
     * 答题详情 JSON
     * 示例: [{"q":1,"a":[2]},{"q":2,"a":[1,3]}]
     */
    @TableField(typeHandler = com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler.class)
    private List<Map<String, Object>> answers;

    @TableField(fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    // ========== 非数据库字段 ==========

    @TableField(exist = false)
    private User user;

    @TableField(exist = false)
    private Assessment assessment;
}