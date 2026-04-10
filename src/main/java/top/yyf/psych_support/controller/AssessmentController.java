package top.yyf.psych_support.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import top.yyf.psych_support.common.Result;
import top.yyf.psych_support.common.ResultCode;
import top.yyf.psych_support.model.dto.AssessmentDetailDTO;
import top.yyf.psych_support.model.dto.AssessmentReportDTO;
import top.yyf.psych_support.model.dto.SubmitAssessmentDTO;
import top.yyf.psych_support.model.vo.AssessmentVO;
import top.yyf.psych_support.model.vo.PageVO;
import top.yyf.psych_support.service.AssessmentService;

import java.util.List;


@Tag(name = "心理测评", description = "心理测评模块相关接口")
@RestController
@RequestMapping("/api/assessments")
@RequiredArgsConstructor
@Slf4j
public class AssessmentController {

    private final AssessmentService assessmentService;

    /**
     * 获取所有可用的心理量表（列表）
     */
    @GetMapping
    @Operation(summary = "获取所有可用心理量表")
    public Result<List<AssessmentVO>> listAssessments() {
        return Result.success(assessmentService.listActiveAssessments());
    }

    /**
     * 获取指定量表的完整详情（含题目和选项）
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取指定量表的完整详情")
    public Result<AssessmentDetailDTO> getAssessmentDetail(@PathVariable Long id) {
        return Result.success(assessmentService.getAssessmentDetail(id));
    }

    /**
     * 提交一份心理测评答卷
     */
    @PostMapping(value = "/{id}/submit", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "提交一份心理测评答卷")
    public Result<AssessmentReportDTO> submitAssessment(
            @PathVariable Long id,
            @Valid @RequestBody SubmitAssessmentDTO request,
            @RequestAttribute("currentUserId") Long currentUserId) { // ✅ 直接注入！
        AssessmentReportDTO report = assessmentService.submitAssessment(currentUserId, id, request);

        if (currentUserId == null) {
            log.error("currentUserId 为空，请检查拦截器是否正常工作");
            return Result.error(ResultCode.UNAUTHORIZED);
        }

        return Result.success(report);
    }

    /**
     * 分页查询当前用户的测评历史记录
     */
    @GetMapping(value = "/my-history", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "分页查询当前用户的测评历史记录")
    public Result<PageVO<AssessmentVO>> getMyHistory(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestAttribute("currentUserId") Long currentUserId) { // ✅ 直接注入！
        PageVO<AssessmentVO> history = assessmentService.getUserAssessmentHistory(currentUserId, page, size);
        return Result.success(history);
    }

    // ========================
    // ⚠️ 你需要补充的方法：从 JWT 获取当前用户 ID
    // ========================

    /**
     * 从请求上下文中获取当前登录用户 ID
     *
     * 实现建议：
     * 方式1：通过 @RequestAttribute 注入（需在拦截器中设置）
     * 方式2：调用 JwtUtils 解析 Authorization 头
     */
    private Long getCurrentUserId() {
        // 示例：如果你在拦截器中设置了 userId 到 request attribute
        // return (Long) RequestContextHolder.currentRequestAttributes()
        //         .getAttribute("userId", RequestAttributes.SCOPE_REQUEST);

        // 或者直接调用 JwtUtils（需注入）
        // String token = ...; // 从 header 获取
        // return jwtUtils.getUserIdFromToken(token);

        throw new UnsupportedOperationException("请根据你的认证体系实现此方法");
    }
}