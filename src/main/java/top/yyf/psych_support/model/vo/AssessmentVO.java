// top/yyf/psych_support/model/vo/AssessmentVO.java
package top.yyf.psych_support.model.vo;

import lombok.Data;

@Data
public class AssessmentVO {
    private Long id;
    private String name;
    private String description;
    private String category;
    private Boolean isActive;
}