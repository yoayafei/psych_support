// 文件路径: top.yyf.psych_support.entity.AppointmentSlot.java
package top.yyf.psych_support.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;

@Data
@TableName("appointment_slots")
public class AppointmentSlot {
    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("counselor_id")
    private Long counselorId; // 关联咨询师

    @TableField("date")
    private LocalDate date; // 日期

    @TableField("start_time")
    private LocalTime startTime; // 开始时间

    @TableField("end_time")
    private LocalTime endTime; // 结束时间

    @TableField("duration_minutes")
    private Integer durationMinutes; // 时长（分钟）

    @TableField("status")
    private Integer status; // AVAILABLE, BOOKED, CANCELLED

    @TableField(value = "max_appointments", fill = FieldFill.INSERT)
    private Integer maxAppointments; // 单时段最大预约数

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}