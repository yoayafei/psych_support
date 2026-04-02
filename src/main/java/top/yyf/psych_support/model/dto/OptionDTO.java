// top/yyf/psych_support/model/dto/OptionDTO.java
package top.yyf.psych_support.model.dto;

import lombok.Data;

@Data
public class OptionDTO {
    private Long id;
    private String content;
    private Integer score;
    private Integer seq;
}