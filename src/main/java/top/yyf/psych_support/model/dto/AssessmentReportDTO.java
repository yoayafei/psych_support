// top/yyf/psych_support/model/dto/AssessmentReportDTO.java
package top.yyf.psych_support.model.dto;

import lombok.Data;

@Data
public class AssessmentReportDTO {
    private Long recordId;      // 测评记录ID
    private String assessmentName;
    private Integer totalScore;
    private String resultLevel;
    private String advice;
    private String completedAt; // 格式化时间
}