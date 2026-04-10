package top.yyf.psych_support.model.vo;


import lombok.Data;

@Data
public class AvailableSlotVO {
    private Long id;
    private String startTime; // "09:00"
    private String endTime;   // "09:50"
    private Boolean isAvailable; // true/false

    public AvailableSlotVO(Long id, String string, String string1, boolean equals) {
    }

    public AvailableSlotVO() {

    }
}