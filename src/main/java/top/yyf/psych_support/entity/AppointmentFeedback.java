// 文件路径: top.yyf.psych_support.entity.AppointmentFeedback.java
package top.yyf.psych_support.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("appointment_feedbacks")
public class AppointmentFeedback {
    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("appointment_id")
    private Long appointmentId; // 关联预约记录

    @TableField("rating")
    private Integer rating; // 评分 1-5

    @TableField("comment")
    private String comment; // 评价内容

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}