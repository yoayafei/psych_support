package top.yyf.psych_support.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonGetter;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("mood_diary")
public class MoodDiary {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String content; // 日记内容

    private String moodTag; // 情绪标签

    private Byte moodLevel; // 情绪强度 1-5

    private String imageUrl; // 图片URL

    private Byte isPublic; // 0=私密,1=待审核,2=已公开

    @TableField(fill = FieldFill.INSERT) // 插入时自动填充
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE) // 插入和更新时都填充
    private LocalDateTime updatedAt;

    @TableLogic // 逻辑删除字段
    @TableField(fill = FieldFill.INSERT)
    private Byte deleted; // 0=未删除, 1=已删除

    // 添加辅助方法
    public Boolean getIsPublicDisplay() {
        return this.isPublic != null && this.isPublic == 2; // 已公开
    }

    public Boolean getIsDeleted() {
        return this.deleted != null && this.deleted == 1;
    }
}