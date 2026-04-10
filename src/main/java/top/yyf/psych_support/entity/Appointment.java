// 文件路径: top.yyf.psych_support.entity.Appointment.java
package top.yyf.psych_support.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;

@Data
@TableName("appointments")
public class Appointment {
    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("user_id")
    private Long userId; // 预约用户

    @TableField("counselor_id")
    private Long counselorId; // 预约咨询师

    @TableField("slot_id")
    private Long slotId; // 关联时间段

    @TableField("date")
    private LocalDate date; // 预约日期

    @TableField("start_time")
    private LocalTime startTime; // 开始时间

    @TableField("end_time")
    private LocalTime endTime; // 结束时间

    @TableField("status")
    private String status; // PENDING, CONFIRMED, etc.

    @TableField("reason_for_appointment")
    private String reasonForAppointment; // 预约原因

    @TableField("notes")
    private String notes; // 备注

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableField("confirmed_at")
    private LocalDateTime confirmedAt; // 确认时间

    @TableField("completed_at")
    private LocalDateTime completedAt; // 完成时间
}