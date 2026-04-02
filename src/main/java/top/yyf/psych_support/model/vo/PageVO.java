// top/yyf/psych_support/model/vo/PageVO.java
package top.yyf.psych_support.model.vo;

import lombok.Data;
import java.util.List;

@Data
public class PageVO<T> {
    private List<T> list;
    private long total;
    private int page;
    private int size;
}