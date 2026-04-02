// top/yyf/psych_support/model/dto/QuestionDTO.java
package top.yyf.psych_support.model.dto;

import lombok.Data;
import java.util.List;

@Data
public class QuestionDTO {
    private Long id;
    private String content;
    private Integer seq;
    private String scoreType;
    private List<OptionDTO> options;
}