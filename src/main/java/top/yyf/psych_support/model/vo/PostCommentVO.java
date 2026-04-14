package top.yyf.psych_support.model.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PostCommentVO {
    private Long id;
    private Long postId;
    private Long userId;
    private String nickname; // 用户昵称
    private String content;
    private Byte status; // 审核状态
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
    private String formattedDate; // 格式化后的日期
    private Boolean isOwner; // 是否为当前用户发表的评论
}