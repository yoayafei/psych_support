// 文件路径: top.yyf.psych_support.vo.CounselorDetailVO.java
package top.yyf.psych_support.model.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import top.yyf.psych_support.entity.Counselor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class CounselorDetailVO {
    private Long id;
    private String name;
    private String title;
    private String qualificationNo;
    private String specialty;
    private String introduction;
    private String profileImageUrl;
    private BigDecimal rating;
    private Integer totalReviews;

    public CounselorDetailVO(Counselor counselor) {
        this.id = counselor.getId();
        this.name = counselor.getName();
        this.title = counselor.getTitle();
        this.qualificationNo = counselor.getQualificationNo();
        this.specialty = counselor.getSpecialty();
        this.introduction = counselor.getIntroduction();
        this.profileImageUrl = counselor.getProfileImageUrl();
        this.rating = counselor.getRating();
        this.totalReviews = counselor.getTotalReviews();
    }
}