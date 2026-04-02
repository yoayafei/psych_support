// top/yyf/psych_support/model/dto/SubmitAssessmentDTO.java
package top.yyf.psych_support.model.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class SubmitAssessmentDTO {
    @NotEmpty(message = "答题不能为空")
    @Valid
    private List<AnswerItem> answers;

    @Data
    public static class AnswerItem {
        private Long questionId;
        private Long optionId;
    }
}