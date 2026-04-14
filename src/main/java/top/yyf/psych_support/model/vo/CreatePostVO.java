package top.yyf.psych_support.model.vo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreatePostVO {
    @NotBlank(message = "帖子标题不能为空")
    @Size(max = 100, message = "帖子标题长度不能超过100个字符")
    private String title;

    @NotBlank(message = "帖子内容不能为空")
    @Size(max = 5000, message = "帖子内容长度不能超过5000个字符")
    private String content;
}