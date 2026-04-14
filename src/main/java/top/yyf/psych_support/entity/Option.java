//package top.yyf.psych_support.entity;
//
//import com.baomidou.mybatisplus.annotation.*;
//import lombok.Data;
//
//@Data
//@TableName("option")
//public class Option {
//
//    @TableId(type = IdType.AUTO)
//    private Long id;
//
//    @TableField("question_id")
//    private Long questionId;
//
//    private String content;
//
//    private Integer score; // 该选项对应的分值
//
//    private Integer seq; // 选项顺序（用于前端排序展示）
//
//    // ========== 非数据库字段（用于关联查询） ==========
//
//    /**
//     * 所属题目信息（非持久化，用于 VO 组装）
//     */
//    @TableField(exist = false)
//    private Question question;
//}