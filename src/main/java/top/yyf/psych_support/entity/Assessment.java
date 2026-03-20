package top.yyf.psych_support.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@TableName("assessment")
public class Assessment {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String title;

    private String description;

    private Integer type; // 0=抑郁, 1=焦虑, 2=压力...

    @TableField("is_active")
    private Boolean active;

    /**
     * 分数解释 JSON 字段
     * 数据库类型：JSON
     * 示例：{"0-4":"无抑郁症状","5-9":"轻度抑郁"}
     */
    @TableField(typeHandler = com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler.class)
    private Map<String, String> scoreExplain;

    @TableField(fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @TableLogic
    private Integer deleted;

    // ========== 非数据库字段（用于前端展示） ==========

    /**
     * 是否启用（语义化布尔值）
     */
    public Boolean getIsActive() {
        return active != null && active;
    }

    /**
     * 隐藏内部字段，避免序列化冗余
     */
    @JsonIgnore
    public Boolean getActive() {
        return active;
    }
}