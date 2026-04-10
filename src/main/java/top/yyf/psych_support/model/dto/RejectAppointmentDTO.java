// 文件路径: top.yyf.psych_support.dto.RejectAppointmentDTO.java
package top.yyf.psych_support.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;


@Data
public class RejectAppointmentDTO {
    @NotBlank
    private String rejectionNote;
}