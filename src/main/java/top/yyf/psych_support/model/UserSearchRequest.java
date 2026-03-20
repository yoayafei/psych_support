// top/yyf/psych_support/model/UserSearchRequest.java
package top.yyf.psych_support.model;

import lombok.Data;

@Data
public class UserSearchRequest {

    /**
     * 搜索关键词（邮箱或昵称）
     */
    private String keyword;

    /**
     * 角色筛选：0=学生, 1=咨询师, 2=管理员
     */
    private Integer role;

    /**
     * 封禁状态：true=已封禁, false=正常, null=不限
     */
    private Boolean banned;

    /**
     * 分页参数
     */
    private int pageNum = 1;
    private int pageSize = 10;
}