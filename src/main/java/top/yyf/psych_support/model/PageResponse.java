// top/yyf/psych_support/model/PageResponse.java
package top.yyf.psych_support.model;

import lombok.Data;
import java.util.List;

@Data
public class PageResponse<T> {

    /**
     * 当前页码（从1开始）
     */
    private int pageNum;

    /**
     * 每页数量
     */
    private int pageSize;

    /**
     * 总记录数
     */
    private long total;

    /**
     * 当前页数据列表
     */
    private List<T> records;

    public PageResponse() {}

    public PageResponse(int pageNum, int pageSize, long total, List<T> records) {
        this.pageNum = pageNum;
        this.pageSize = pageSize;
        this.total = total;
        this.records = records;
    }
}