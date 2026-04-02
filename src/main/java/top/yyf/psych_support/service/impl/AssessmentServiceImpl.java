// top/yyf/psych_support/service/impl/AssessmentServiceImpl.java
package top.yyf.psych_support.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.yyf.psych_support.entity.*;
import top.yyf.psych_support.mapper.*;
import top.yyf.psych_support.model.dto.*;
import top.yyf.psych_support.model.vo.AssessmentVO;
import top.yyf.psych_support.model.vo.PageVO;
import top.yyf.psych_support.service.AssessmentService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AssessmentServiceImpl implements AssessmentService {

    private final AssessmentMapper assessmentMapper;
    private final AssessmentQuestionMapper questionMapper;
    private final AssessmentOptionMapper optionMapper;
    private final UserAssessmentMapper userAssessmentMapper;

    @Override
    public List<AssessmentVO> listActiveAssessments() {
        return assessmentMapper.selectList(
                new LambdaQueryWrapper<Assessment>()
                        .eq(Assessment::getIsActive, true)
                        .eq(Assessment::getDeleted, false)
        ).stream().map(this::convertToVO).collect(Collectors.toList());
    }

    @Override
    public AssessmentDetailDTO getAssessmentDetail(Long assessmentId) {
        Assessment assessment = assessmentMapper.selectById(assessmentId);
        if (assessment == null || Boolean.FALSE.equals(assessment.getIsActive()) || Boolean.TRUE.equals(assessment.getDeleted())) {
            throw new RuntimeException("量表不存在或已停用");
        }

        // 查询题目
        List<AssessmentQuestion> questions = questionMapper.selectList(
                new LambdaQueryWrapper<AssessmentQuestion>()
                        .eq(AssessmentQuestion::getAssessmentId, assessmentId)
                        .eq(AssessmentQuestion::getDeleted, false)
                        .orderByAsc(AssessmentQuestion::getSeq)
        );

        // 查询所有选项（批量）
        List<Long> questionIds = questions.stream().map(AssessmentQuestion::getId).collect(Collectors.toList());
        List<AssessmentOption> allOptions = new ArrayList<>();
        if (!questionIds.isEmpty()) {
            allOptions = optionMapper.selectList(
                    new LambdaQueryWrapper<AssessmentOption>()
                            .in(AssessmentOption::getQuestionId, questionIds)
                            .eq(AssessmentOption::getDeleted, false)
                            .orderByAsc(AssessmentOption::getSeq)
            );
        }

        // 按 questionId 分组选项
        Map<Long, List<AssessmentOption>> optionsByQuestion = allOptions.stream()
                .collect(Collectors.groupingBy(AssessmentOption::getQuestionId));

        // 构建 DTO
        AssessmentDetailDTO dto = new AssessmentDetailDTO();
        dto.setId(assessment.getId());
        dto.setName(assessment.getName());
        dto.setInstructions(assessment.getInstructions());
        dto.setCategory(assessment.getCategory());

        dto.setQuestions(questions.stream().map(q -> {
            QuestionDTO qDto = new QuestionDTO();
            qDto.setId(q.getId());
            qDto.setContent(q.getContent());
            qDto.setSeq(q.getSeq());
            qDto.setScoreType(q.getScoreType());
            qDto.setOptions(optionsByQuestion.getOrDefault(q.getId(), Collections.emptyList())
                    .stream().map(o -> {
                        OptionDTO oDto = new OptionDTO();
                        oDto.setId(o.getId());
                        oDto.setContent(o.getContent());
                        oDto.setScore(o.getScore());
                        oDto.setSeq(o.getSeq());
                        return oDto;
                    }).collect(Collectors.toList()));
            return qDto;
        }).collect(Collectors.toList()));

        return dto;
    }

    @Override
    @Transactional
    public AssessmentReportDTO submitAssessment(Long userId, Long assessmentId, SubmitAssessmentDTO request) {
        // 1. 验证量表存在且启用
        Assessment assessment = assessmentMapper.selectById(assessmentId);
        if (assessment == null || !Boolean.TRUE.equals(assessment.getIsActive())) {
            throw new RuntimeException("量表不可用");
        }

        // 2. 验证题目和选项合法性
        List<Long> questionIds = request.getAnswers().stream()
                .map(SubmitAssessmentDTO.AnswerItem::getQuestionId)
                .collect(Collectors.toList());
        List<AssessmentQuestion> validQuestions = questionMapper.selectList(
                new LambdaQueryWrapper<AssessmentQuestion>()
                        .eq(AssessmentQuestion::getAssessmentId, assessmentId)
                        .in(AssessmentQuestion::getId, questionIds)
                        .eq(AssessmentQuestion::getDeleted, false)
        );
        if (validQuestions.size() != questionIds.size()) {
            throw new RuntimeException("包含无效题目");
        }

        // 3. 计算总分
        int totalScore = 0;
        List<UserAssessment.AnswerItem> rawAnswers = new ArrayList<>();
        for (SubmitAssessmentDTO.AnswerItem answer : request.getAnswers()) {
            // 查找对应选项
            AssessmentOption option = optionMapper.selectOne(
                    new LambdaQueryWrapper<AssessmentOption>()
                            .eq(AssessmentOption::getQuestionId, answer.getQuestionId())
                            .eq(AssessmentOption::getId, answer.getOptionId())
                            .eq(AssessmentOption::getDeleted, false)
            );
            if (option == null) {
                throw new RuntimeException("题目 " + answer.getQuestionId() + " 的选项无效");
            }
            totalScore += option.getScore();
            UserAssessment.AnswerItem item = new UserAssessment.AnswerItem();
            item.setQuestionId(answer.getQuestionId());
            item.setOptionId(answer.getOptionId());
            rawAnswers.add(item);
        }

        // 4. 匹配结果等级
        String resultLevel = "未知";
        String advice = "请咨询专业人员";
        if (assessment.getScoringRules() != null && assessment.getScoringRules().getRanges() != null) {
            for (Assessment.ScoringRules.Range range : assessment.getScoringRules().getRanges()) {
                if (totalScore >= range.getMin() && totalScore <= range.getMax()) {
                    resultLevel = range.getLevel();
                    advice = range.getAdvice();
                    break;
                }
            }
        }

        // 5. 保存记录
        UserAssessment record = new UserAssessment();
        record.setUserId(userId);
        record.setAssessmentId(assessmentId);
        record.setTotalScore(totalScore);
        record.setResultLevel(resultLevel);
        record.setRawData(rawAnswers);
        record.setCompletedAt(LocalDateTime.now());
        userAssessmentMapper.insert(record);

        // 6. 构建返回
        AssessmentReportDTO report = new AssessmentReportDTO();
        report.setRecordId(record.getId());
        report.setAssessmentName(assessment.getName());
        report.setTotalScore(totalScore);
        report.setResultLevel(resultLevel);
        report.setAdvice(advice);
        report.setCompletedAt(record.getCompletedAt()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));

        return report;
    }

    @Override
    public PageVO<AssessmentVO> getUserAssessmentHistory(Long userId, int page, int size) {
        IPage<UserAssessment> pageData = userAssessmentMapper.selectPage(
                new Page<>(page, size),
                new LambdaQueryWrapper<UserAssessment>()
                        .eq(UserAssessment::getUserId, userId)
                        .orderByDesc(UserAssessment::getCompletedAt)
        );

        // 关联量表名称
        List<Long> assessmentIds = pageData.getRecords().stream()
                .map(UserAssessment::getAssessmentId)
                .distinct()
                .collect(Collectors.toList());
        Map<Long, String> assessmentNameMap;
        if (!assessmentIds.isEmpty()) {
            List<Assessment> assessments = assessmentMapper.selectBatchIds(assessmentIds);
            assessmentNameMap = assessments.stream()
                    .collect(Collectors.toMap(Assessment::getId, Assessment::getName));
        } else {
            assessmentNameMap = new HashMap<>();
        }

        List<AssessmentVO> vos = pageData.getRecords().stream().map(record -> {
            AssessmentVO vo = new AssessmentVO();
            vo.setId(record.getId()); // 注意：这里 id 是 recordId，不是 assessmentId
            vo.setName(assessmentNameMap.getOrDefault(record.getAssessmentId(), "未知量表"));
            vo.setDescription("得分: " + record.getTotalScore() + " (" + record.getResultLevel() + ")");
            vo.setCategory(""); // 可扩展
            return vo;
        }).collect(Collectors.toList());

        PageVO<AssessmentVO> result = new PageVO<>();
        result.setList(vos);
        result.setTotal(pageData.getTotal());
        result.setPage(page);
        result.setSize(size);
        return result;
    }

    // --- 辅助方法 ---
    private AssessmentVO convertToVO(Assessment assessment) {
        AssessmentVO vo = new AssessmentVO();
        vo.setId(assessment.getId());
        vo.setName(assessment.getName());
        vo.setDescription(assessment.getDescription());
        vo.setCategory(assessment.getCategory());
        return vo;
    }
}