package top.yyf.psych_support.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonGetter;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("user")
public class User {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String email;

    private String password;

    private String nickname;

    private String avatar;

    private Integer role; // 0=学生, 1=咨询师, 2=管理员

    private String college;

    private String grade;

    @TableField("is_banned")
    private Boolean banned; // 数据库是 TINYINT(1)，Java 用 Boolean 更语义化

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    @TableLogic
    private Integer deleted;

    // ========== 计算属性（非数据库字段） ==========

    // 角色名称（用于前端显示）
    @JsonGetter("roleName")
    public String getRoleName() {
        if (role == null) {
            return "未知";
        }
        switch (role) {
            case 0: return "学生";
            case 1: return "咨询师";
            case 2: return "管理员";
            default: return "未知";
        }
    }

    // 是否被封禁（布尔语义）
    @JsonGetter("isBanned")
    public Boolean getIsBanned() {
        return banned != null && banned;
    }
}