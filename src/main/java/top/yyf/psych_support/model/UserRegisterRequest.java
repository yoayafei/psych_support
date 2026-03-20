// top/yyf/psych_support/model/UserRegisterRequest.java
package top.yyf.psych_support.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UserRegisterRequest {
    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    private String email;

    @NotBlank(message = "密码不能为空")
    private String password; // 新增

    @NotBlank(message = "昵称不能为空")
    private String nickname;

    private String college;
    private String grade;
}