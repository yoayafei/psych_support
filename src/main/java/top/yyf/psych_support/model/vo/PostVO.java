package top.yyf.psych_support.model.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PostVO {
    private Long id;
    private Long userId;
    private String nickname; // 用户昵称
    private String title;
    private String content;
    private Byte status; // 0=拒绝,1=待审核,2=通过
    private Integer viewCount;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
    private String formattedDate; // 格式化后的日期
    private Integer commentCount; // 评论数量
    private Boolean isOwner; // 是否为当前用户发布的帖子
}