// 文件路径: top.yyf.psych_support.controller.CounselorController.java
package top.yyf.psych_support.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import top.yyf.psych_support.common.Result;
import top.yyf.psych_support.model.vo.AvailableSlotVO;
import top.yyf.psych_support.model.vo.CounselorDetailVO;
import top.yyf.psych_support.service.ICounselorService;

import java.time.LocalDate;
import java.util.List;

@Tag(name = "咨询师管理", description = "咨询师信息及排班查询接口")
@RestController
@RequestMapping("/api/counselors")
@RequiredArgsConstructor
public class CounselorController {

    private final ICounselorService counselorService;

    @Operation(summary = "获取所有可用咨询师")
    @GetMapping
    public Result<List<CounselorDetailVO>> listCounselors() {
        var counselors = counselorService.getAllAvailableCounselors();
        var voList = counselors.stream()
                .map(CounselorDetailVO::new)
                .toList();
        return Result.success(voList);
    }

    @Operation(summary = "获取咨询师详情")
    @GetMapping("/{id}")
    public Result<CounselorDetailVO> getCounselorDetail(@PathVariable Long id) {
        var detail = counselorService.getCounselorDetail(id);
        return Result.success(detail);
    }

    @Operation(summary = "获取咨询师某天的可预约时间段")
    @GetMapping("/{id}/slots")
    public Result<List<AvailableSlotVO>> getAvailableSlots(@PathVariable Long id) {
        List<AvailableSlotVO> slots = counselorService.getAvailableSlots(id);
        return Result.success(slots);
    }
}