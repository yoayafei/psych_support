package top.yyf.psych_support.model.vo;


import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class AvailableSlotVO {
    private Long slotId;           // 时间段模板ID
    private Long counselorId;
    private String counselorName;
    private LocalDate date;        // 预约日期
    private LocalTime startTime;
    private LocalTime endTime;
    private Boolean isAvailable;   // true=可预约, false=已被预约

    public AvailableSlotVO() {
    }

    public AvailableSlotVO(Long slotId, LocalTime startTime, LocalTime endTime, boolean isAvailable) {
        this.slotId = slotId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.isAvailable = isAvailable;
    }
}