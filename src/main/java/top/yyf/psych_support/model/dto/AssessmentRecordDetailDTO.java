package top.yyf.psych_support.model.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class AssessmentRecordDetailDTO {
    private Long recordId;
    private String assessmentName;
    private Integer totalScore;
    private String resultLevel;
    private LocalDateTime completedAt;
    private List<AnswerDetail> answers;

    @Data
    public static class AnswerDetail {
        private Integer questionSeq;
        private String questionContent;
        private String selectedOption;
        private Integer score;
    }
}