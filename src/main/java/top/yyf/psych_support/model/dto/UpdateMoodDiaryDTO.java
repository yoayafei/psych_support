package top.yyf.psych_support.model.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;



@Data
public class UpdateMoodDiaryDTO {
    private String content;

    private String moodTag;

    @Min(value = 1, message = "情绪强度必须在1-5之间")
    @Max(value = 5, message = "情绪强度必须在1-5之间")
    private Byte moodLevel;

    private String imageUrl;

    @Min(value = 0, message = "公开状态必须是0、1或2")
    @Max(value = 2, message = "公开状态必须是0、1或2")
    private Byte isPublic;
}
