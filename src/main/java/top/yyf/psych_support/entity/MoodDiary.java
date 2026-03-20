package top.yyf.psych_support.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonGetter;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("mood_diary")
public class MoodDiary {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("user_id")
    private Long userId;

    private String content;

    @TableField("mood_tag")
    private String moodTag;

    @TableField("mood_level")
    private Integer moodLevel; // 1-5 强度

    @TableField("image_url")
    private String imageUrl;

    @TableField("is_public")
    private Integer isPublic; // 0=私密, 1=待审核, 2=已公开

    @TableField(fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    // ========== 非数据库字段 ==========

    @TableField(exist = false)
    private User user;

    /**
     * 公开状态语义化（用于前端）
     */
    @JsonGetter("publicStatus")
    public String getPublicStatus() {
        if (isPublic == null) {
            return "未知";
        }
        switch (isPublic) {
            case 0: return "私密";
            case 1: return "审核中";
            case 2: return "已公开";
            default: return "未知";
        }
    }
}