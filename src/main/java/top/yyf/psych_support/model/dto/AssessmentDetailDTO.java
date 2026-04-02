// top/yyf/psych_support/model/dto/AssessmentDetailDTO.java
package top.yyf.psych_support.model.dto;

import lombok.Data;
import java.util.List;

@Data
public class AssessmentDetailDTO {
    private Long id;
    private String name;
    private String instructions;
    private String category;
    private List<QuestionDTO> questions;
}