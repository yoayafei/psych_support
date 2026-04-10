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
    private Long counselorId;
    private String counselorName;
    private String counselorTitle;
    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;
    private String status; // PENDING, CONFIRMED, etc.
    private String reasonForAppointment;
    private String notes;
    private LocalDateTime createdAt;

    public void setUserName(String nickname) {
    }
}