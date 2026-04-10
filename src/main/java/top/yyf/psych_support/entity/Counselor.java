// 文件路径: top.yyf.psych_support.entity.Counselor.java
package top.yyf.psych_support.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("counselors")
public class Counselor {
    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("user_id")
    private Long userId; // 关联用户表

    @TableField("name")
    private String name; // 咨询师真实姓名

    @TableField("title")
    private String title; // 职称

    @TableField("qualification_no")
    private String qualificationNo; // 资格证书编号

    @TableField("specialty")
    private String specialty; // 擅长领域

    @TableField("introduction")
    private String introduction; // 个人介绍

    @TableField("profile_image_url")
    private String profileImageUrl; // 头像URL

    @TableField("rating")
    private BigDecimal rating; // 平均评分

    @TableField("total_reviews")
    private Integer totalReviews; // 评价总数

    @TableField(value = "is_available", fill = FieldFill.INSERT)
    private Boolean isAvailable; // 是否在职

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}