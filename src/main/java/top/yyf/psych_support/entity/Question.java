//package top.yyf.psych_support.entity;
//
//import com.baomidou.mybatisplus.annotation.*;
//import lombok.Data;
//
//@Data
//@TableName("question")
//public class Question {
//
//    @TableId(type = IdType.AUTO)
//    private Long id;
//
//    @TableField("assessment_id")
//    private Long assessmentId;
//
//    private String content;
//
//    private Integer seq; // 题目顺序
//
//    private Integer type; // 0=单选, 1=多选, 2=量表题(1-5)
//
//    // ========== 非数据库字段（用于关联查询） ==========
//
//    /**
//     * 所属量表信息（非持久化，用于 VO 组装）
//     */
//    @TableField(exist = false)
//    private Assessment assessment;
//
//    /**
//     * 题目选项列表（非持久化，用于前端渲染）
//     */
//    @TableField(exist = false)
//    private java.util.List<Option> options;
//}