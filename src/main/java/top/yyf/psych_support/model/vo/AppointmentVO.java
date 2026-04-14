package top.yyf.psych_support.model.vo;

import lombok.Data;

import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Date;

@Data
public class AppointmentVO {
    private Long id;
    private Long userId;           // ✅ 添加用户ID字段
    private Long counselorId;
    private String counselorName;
    private String counselorTitle;
    private String userName;       // ✅ 添加用户名字段（存储 nickname）
    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;
    private String status;
    private String reasonForAppointment;
    private String notes;
    private LocalDateTime createdAt;

}