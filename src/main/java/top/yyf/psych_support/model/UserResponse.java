// top/yyf/psych_support/model/UserResponse.java
package top.yyf.psych_support.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserResponse {

    private Long id;

    private String email;

    private String nickname;

    private String avatar;

    private Integer role; // 原始角色值（0/1/2）

    private String roleName; // 计算字段：学生/咨询师/管理员

    private String college;

    private String grade;

    private Boolean banned; // 是否被封禁（true/false）

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
}