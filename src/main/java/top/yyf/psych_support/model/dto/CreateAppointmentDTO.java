package top.yyf.psych_support.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateAppointmentDTO {
    @NotNull
    private Long counselorId; // 咨询师ID
    
    @NotNull
    private Long slotId;      // 时间段ID
    
    @NotBlank
    private String reason;    // 预约原因
    
    private String notes;     // 备注
}