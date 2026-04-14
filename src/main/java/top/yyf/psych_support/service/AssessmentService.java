// top/yyf/psych_support/service/AssessmentService.java
package top.yyf.psych_support.service;

import top.yyf.psych_support.entity.Assessment;
import top.yyf.psych_support.model.dto.AssessmentDetailDTO;
import top.yyf.psych_support.model.dto.AssessmentRecordDetailDTO;
import top.yyf.psych_support.model.dto.AssessmentReportDTO;
import top.yyf.psych_support.model.dto.SubmitAssessmentDTO;
import top.yyf.psych_support.model.vo.AssessmentVO;
import top.yyf.psych_support.model.vo.PageVO;

import java.util.List;

public interface AssessmentService {

    /**
     * 获取所有启用的量表（用于首页展示）
     */
    List<AssessmentVO> listActiveAssessments();

    /**
     * 获取量表详情（含题目和选项）
     */
    AssessmentDetailDTO getAssessmentDetail(Long assessmentId);

    /**
     * 提交测评答卷
     */
    AssessmentReportDTO submitAssessment(Long userId, Long assessmentId, SubmitAssessmentDTO request);

    /**
     * 分页查询用户测评历史
     */
    PageVO<AssessmentVO> getUserAssessmentHistory(Long userId, int page, int size);

    // 在 AssessmentService.java 中添加
    AssessmentRecordDetailDTO getAssessmentRecordDetail(Long recordId, Long userId);
}