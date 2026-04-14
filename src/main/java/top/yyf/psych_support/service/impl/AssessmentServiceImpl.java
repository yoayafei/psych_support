// top/yyf/psych_support/service/impl/AssessmentServiceImpl.java
package top.yyf.psych_support.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
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
    private final UserMapper userMapper; // 添加 UserMapper

    @Override
    public List<AssessmentVO> listActiveAssessments() {
        return assessmentMapper.selectList(
                new LambdaQueryWrapper<Assessment>()
                        .eq(Assessment::getIsActive, true)
                        .eq(Assessment::getDeleted, false)
        ).stream().map(this::convertToVO).collect(Collectors.toList());
    }

    @Override
    @Cacheable(value = "assessment", key = "#assessmentId")
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

        if (questions.isEmpty()) {
            throw new RuntimeException("该量表暂无题目");
        }

        // 查询所有选项（批量）
        List<Long> questionIds = questions.stream()
                .map(AssessmentQuestion::getId)
                .collect(Collectors.toList());

        List<AssessmentOption> allOptions = optionMapper.selectList(
                new LambdaQueryWrapper<AssessmentOption>()
                        .in(AssessmentOption::getQuestionId, questionIds)
                        .eq(AssessmentOption::getDeleted, false)
                        .orderByAsc(AssessmentOption::getSeq)
        );

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

            List<AssessmentOption> options = optionsByQuestion.getOrDefault(q.getId(), Collections.emptyList());
            qDto.setOptions(options.stream().map(o -> {
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
        // 1. 验证用户是否存在
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        // 2. 验证量表存在且启用
        Assessment assessment = assessmentMapper.selectById(assessmentId);
        if (assessment == null || !Boolean.TRUE.equals(assessment.getIsActive())) {
            throw new RuntimeException("量表不可用");
        }

        // 3. 验证是否已经做过该测评（可选，根据业务需求）
        // 如果需要限制每个用户只能做一次，取消下面的注释
        /*
        Long count = userAssessmentMapper.selectCount(
            new LambdaQueryWrapper<UserAssessment>()
                .eq(UserAssessment::getUserId, userId)
                .eq(UserAssessment::getAssessmentId, assessmentId)
        );
        if (count > 0) {
            throw new RuntimeException("您已经完成过该测评，不能重复提交");
        }
        */

        // 4. 获取该量表的所有题目
        List<AssessmentQuestion> validQuestions = questionMapper.selectList(
                new LambdaQueryWrapper<AssessmentQuestion>()
                        .eq(AssessmentQuestion::getAssessmentId, assessmentId)
                        .eq(AssessmentQuestion::getDeleted, false)
        );

        Map<Long, AssessmentQuestion> questionMap = validQuestions.stream()
                .collect(Collectors.toMap(AssessmentQuestion::getId, q -> q));

        // 5. 验证提交的题目数量是否匹配
        if (request.getAnswers().size() != validQuestions.size()) {
            throw new RuntimeException("题目数量不匹配，请完整作答");
        }

        // 6. 计算总分并验证选项合法性
        int totalScore = 0;
        List<UserAssessment.AnswerItem> rawAnswers = new ArrayList<>();

        for (SubmitAssessmentDTO.AnswerItem answer : request.getAnswers()) {
            // 验证题目是否存在
            if (!questionMap.containsKey(answer.getQuestionId())) {
                throw new RuntimeException("题目 " + answer.getQuestionId() + " 不属于该量表");
            }

            // 验证选项是否存在并获取分值
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

        log.info("rawAnswers 大小: {}, 内容: {}", rawAnswers.size(), rawAnswers);

        // 7. 匹配结果等级和建议
        String resultLevel = "未知";
        String advice = "请咨询专业人员获取详细建议";

        if (assessment.getScoringRules() != null && assessment.getScoringRules().getRanges() != null) {
            for (Assessment.ScoringRules.Range range : assessment.getScoringRules().getRanges()) {
                if (totalScore >= range.getMin() && totalScore <= range.getMax()) {
                    resultLevel = range.getLevel();
                    advice = range.getAdvice();
                    break;
                }
            }
        }

        // 8. 保存记录
        UserAssessment record = new UserAssessment();
        record.setUserId(userId);
        record.setAssessmentId(assessmentId);
        record.setTotalScore(totalScore);
        record.setResultLevel(resultLevel);
        record.setRawData(rawAnswers);
        record.setCompletedAt(LocalDateTime.now());
        userAssessmentMapper.insert(record);

        log.info("用户 {} 完成了测评 {}，得分：{}，等级：{}", userId, assessment.getName(), totalScore, resultLevel);

        // 9. 构建返回
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
        // 验证用户是否存在
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        IPage<UserAssessment> pageData = userAssessmentMapper.selectPage(
                new Page<>(page, size),
                new LambdaQueryWrapper<UserAssessment>()
                        .eq(UserAssessment::getUserId, userId)
                        .orderByDesc(UserAssessment::getCompletedAt)
        );

        if (pageData.getRecords().isEmpty()) {
            PageVO<AssessmentVO> emptyResult = new PageVO<>();
            emptyResult.setList(Collections.emptyList());
            emptyResult.setTotal(0L);
            emptyResult.setPage(page);
            emptyResult.setSize(size);
            return emptyResult;
        }

        // 关联量表信息
        List<Long> assessmentIds = pageData.getRecords().stream()
                .map(UserAssessment::getAssessmentId)
                .distinct()
                .collect(Collectors.toList());

        Map<Long, Assessment> assessmentMap;
        if (!assessmentIds.isEmpty()) {
            List<Assessment> assessments = assessmentMapper.selectBatchIds(assessmentIds);
            assessmentMap = assessments.stream()
                    .collect(Collectors.toMap(Assessment::getId, a -> a));
        } else {
            assessmentMap = new HashMap<>();
        }

        List<AssessmentVO> vos = pageData.getRecords().stream().map(record -> {
            AssessmentVO vo = new AssessmentVO();
            // 注意：这里使用 recordId 作为标识，前端需要区分
            vo.setId(record.getId()); // 这是记录ID
            vo.setName(assessmentMap.getOrDefault(record.getAssessmentId(), new Assessment()).getName());
            // 添加得分和等级信息到描述中
            vo.setDescription(String.format("得分: %d (%s) - %s",
                    record.getTotalScore(),
                    record.getResultLevel(),
                    record.getCompletedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))));
            vo.setCategory(assessmentMap.getOrDefault(record.getAssessmentId(), new Assessment()).getCategory());
            return vo;
        }).collect(Collectors.toList());

        PageVO<AssessmentVO> result = new PageVO<>();
        result.setList(vos);
        result.setTotal(pageData.getTotal());
        result.setPage(page);
        result.setSize(size);
        return result;
    }

    /**
     * 获取测评记录的详细信息（包含答题内容）
     */
    @Override
    public AssessmentRecordDetailDTO getAssessmentRecordDetail(Long recordId, Long userId) {
        UserAssessment record = userAssessmentMapper.selectById(recordId);
        if (record == null) {
            throw new RuntimeException("测评记录不存在");
        }

        // 验证权限：只有本人可以查看
        if (!record.getUserId().equals(userId)) {
            throw new RuntimeException("无权查看他人的测评记录");
        }

        Assessment assessment = assessmentMapper.selectById(record.getAssessmentId());
        if (assessment == null) {
            throw new RuntimeException("量表信息不存在");
        }

        // 获取所有题目信息
        List<AssessmentQuestion> questions = questionMapper.selectList(
                new LambdaQueryWrapper<AssessmentQuestion>()
                        .eq(AssessmentQuestion::getAssessmentId, assessment.getId())
                        .eq(AssessmentQuestion::getDeleted, false)
                        .orderByAsc(AssessmentQuestion::getSeq)
        );

        Map<Long, AssessmentQuestion> questionMap = questions.stream()
                .collect(Collectors.toMap(AssessmentQuestion::getId, q -> q));

        // 获取所有选项信息
        List<Long> questionIds = questions.stream()
                .map(AssessmentQuestion::getId)
                .collect(Collectors.toList());

        List<AssessmentOption> allOptions = optionMapper.selectList(
                new LambdaQueryWrapper<AssessmentOption>()
                        .in(AssessmentOption::getQuestionId, questionIds)
                        .eq(AssessmentOption::getDeleted, false)
        );

        Map<Long, AssessmentOption> optionMap = allOptions.stream()
                .collect(Collectors.toMap(AssessmentOption::getId, o -> o));

        // 构建详细记录
        AssessmentRecordDetailDTO detail = new AssessmentRecordDetailDTO();
        detail.setRecordId(record.getId());
        detail.setAssessmentName(assessment.getName());
        detail.setTotalScore(record.getTotalScore());
        detail.setResultLevel(record.getResultLevel());
        detail.setCompletedAt(record.getCompletedAt());

        // 构建答题详情
        List<AssessmentRecordDetailDTO.AnswerDetail> answerDetails = new ArrayList<>();
        for (UserAssessment.AnswerItem answer : record.getRawData()) {
            AssessmentRecordDetailDTO.AnswerDetail answerDetail = new AssessmentRecordDetailDTO.AnswerDetail();
            AssessmentQuestion question = questionMap.get(answer.getQuestionId());
            if (question != null) {
                answerDetail.setQuestionContent(question.getContent());
                answerDetail.setQuestionSeq(question.getSeq());
            }

            AssessmentOption option = optionMap.get(answer.getOptionId());
            if (option != null) {
                answerDetail.setSelectedOption(option.getContent());
                answerDetail.setScore(option.getScore());
            }

            answerDetails.add(answerDetail);
        }

        // 按序号排序
        answerDetails.sort(Comparator.comparing(AssessmentRecordDetailDTO.AnswerDetail::getQuestionSeq));
        detail.setAnswers(answerDetails);

        return detail;
    }

    // --- 辅助方法 ---
    private AssessmentVO convertToVO(Assessment assessment) {
        AssessmentVO vo = new AssessmentVO();
        vo.setId(assessment.getId());
        vo.setName(assessment.getName());
        vo.setDescription(assessment.getDescription());
        vo.setCategory(assessment.getCategory());
        vo.setIsActive(assessment.getIsActive());  // ✅ 设置 isActive
        return vo;
    }
}