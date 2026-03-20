package top.yyf.psych_support.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonGetter;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("post")
public class Post {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("user_id")
    private Long userId;

    private String title;

    private String content;

    private Integer status; // 0=拒绝, 1=待审核, 2=通过

    @TableField("view_count")
    private Integer viewCount;

    @TableField(fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    // ========== 非数据库字段 ==========

    @TableField(exist = false)
    private User user;

    @TableField(exist = false)
    private Integer likeCount; // 点赞数（需额外查询）

    @TableField(exist = false)
    private Integer commentCount; // 评论数（需额外查询）

    /**
     * 审核状态语义化
     */
    @JsonGetter("statusText")
    public String getStatusText() {
        if (status == null) {
            return "未知";
        }
        switch (status) {
            case 0: return "已拒绝";
            case 1: return "审核中";
            case 2: return "已通过";
            default: return "未知";
        }
    }
}