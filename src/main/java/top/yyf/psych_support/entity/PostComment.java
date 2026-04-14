package top.yyf.psych_support.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("post_comment")
public class PostComment {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long postId; // 关联帖子ID

    private Long userId; // 评论用户ID

    private String content; // 评论内容

    @TableField("`status`") // 使用反引号避免与SQL关键字冲突
    private Byte status; // 审核状态

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;


    @TableLogic
    @TableField(fill = FieldFill.INSERT)
    private Byte deleted; // 逻辑删除字段

    // 辅助方法
    public Boolean getIsApproved() {
        return this.status != null && this.status == 2; // 已通过审核
    }

    public Boolean getIsPending() {
        return this.status != null && this.status == 1; // 待审核
    }

    public Boolean getIsRejected() {
        return this.status != null && this.status == 0; // 已拒绝
    }
}