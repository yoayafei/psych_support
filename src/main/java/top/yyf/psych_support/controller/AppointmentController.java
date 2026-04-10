// 文件路径: top.yyf.psych_support.controller.AppointmentController.java
package top.yyf.psych_support.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import top.yyf.psych_support.common.Result;
import top.yyf.psych_support.entity.Counselor;
import top.yyf.psych_support.mapper.CounselorMapper;
import top.yyf.psych_support.model.dto.CreateAppointmentDTO;
import top.yyf.psych_support.model.dto.RejectAppointmentDTO;
import top.yyf.psych_support.model.vo.AppointmentVO;
import top.yyf.psych_support.service.IAppointmentService;
import top.yyf.psych_support.util.JwtUtils;

import java.time.LocalDate;

@Tag(name = "预约管理", description = "心理咨询预约相关接口")
@RestController
@RequestMapping("/api/appointments")
@RequiredArgsConstructor
public class AppointmentController {

    private final IAppointmentService appointmentService;
    private final JwtUtils jwtUtils;
    private final CounselorMapper counselorMapper;


    @Operation(summary = "创建预约申请")
    @PostMapping
    public Result<AppointmentVO> createAppointment(
            @RequestBody CreateAppointmentDTO dto,
            @RequestHeader("Authorization") String token,
            HttpServletRequest request) {

        Long userId = jwtUtils.getUserIdFromToken(token);
        var result = appointmentService.createAppointment(dto, userId);
        return Result.success(result);
    }

    @Operation(summary = "获取我的预约记录")
    @GetMapping("/my")
    public Result<?> getMyAppointments(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestHeader("Authorization") String token,
            HttpServletRequest request) {

        Long userId = jwtUtils.getUserIdFromToken(token);
        var result = appointmentService.getMyAppointments(page, size, userId);
        return Result.success(result);
    }

    @Operation(summary = "获取预约详情")
    @GetMapping("/{id}")
    public Result<AppointmentVO> getAppointmentDetail(
            @PathVariable Long id,
            @RequestHeader("Authorization") String token,
            HttpServletRequest request) {
        Long currentUserId = jwtUtils.getUserIdFromToken(token);
//        Long currentUserId = (Long) request.getAttribute("userId");
        var detail = appointmentService.getAppointmentDetail(id, currentUserId);
        return Result.success(detail);
    }

    @Operation(summary = "用户取消预约")
    @PutMapping("/{id}/cancel")
    public Result<String> cancelAppointmentByUser(
            @PathVariable Long id,
            @RequestHeader("Authorization") String token,
            HttpServletRequest request) {
        
        Long userId = jwtUtils.getUserIdFromToken(token);
        String result = appointmentService.cancelAppointmentByUser(id, userId);
        return Result.success(result);
    }

    // --- 管理员/咨询师专属接口 ---
    // 这些接口也需要获取操作者ID，但权限控制由拦截器处理

    @Operation(summary = "管理员/咨询师确认预约")
    @PutMapping("/{id}/confirm")
    public Result<String> confirmAppointment(@PathVariable Long id, @RequestHeader("Authorization") String token,HttpServletRequest request) {
        // 可以获取操作者ID用于日志记录等
        Long operatorId = jwtUtils.getUserIdFromToken(token);
        String result = appointmentService.confirmAppointment(id);
        return Result.success(result);
    }

    @Operation(summary = "管理员/咨询师拒绝预约")
    @PutMapping("/{id}/reject")
    public Result<String> rejectAppointment(
            @PathVariable Long id,
            @RequestBody RejectAppointmentDTO dto,
            HttpServletRequest request) {
        
        Long operatorId = (Long) request.getAttribute("userId");
        String result = appointmentService.rejectAppointment(id, dto);
        return Result.success(result);
    }

    @Operation(summary = "管理员/咨询师标记预约完成")
    @PutMapping("/{id}/complete")
    public Result<String> completeAppointment(@PathVariable Long id, HttpServletRequest request) {
        Long operatorId = (Long) request.getAttribute("userId");
        String result = appointmentService.completeAppointment(id);
        return Result.success(result);
    }

    @Operation(summary = "获取咨询师的预约记录")
    @GetMapping("/counselor")
    public Result<?> getCounselorAppointments(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestHeader("Authorization") String token) {

        Long userId = jwtUtils.getUserIdFromToken(token);

        // 根据用户ID查找咨询师ID
        LambdaQueryWrapper<Counselor> counselorWrapper = new LambdaQueryWrapper<Counselor>()
                .eq(Counselor::getUserId, userId); // 假设Counselor表有user_id字段关联
        Counselor counselor = counselorMapper.selectOne(counselorWrapper);

        if (counselor == null) {
            return Result.error("当前用户不是咨询师");
        }

        Long counselorId = counselor.getId(); // 获取咨询师表的ID
        var result = appointmentService.getCounselorAppointments(page, size, counselorId);
        return Result.success(result);
    }

    @Operation(summary = "获取咨询师某天的预约安排")
    @GetMapping("/counselor/schedule")
    public Result<?> getCounselorSchedule(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) LocalDate date,
            @RequestHeader("Authorization") String token) {

        if (date == null) {
            date = LocalDate.now();
        }

        Long userId = jwtUtils.getUserIdFromToken(token);

        // 根据用户ID查找咨询师ID
        LambdaQueryWrapper<Counselor> counselorWrapper = new LambdaQueryWrapper<Counselor>()
                .eq(Counselor::getUserId, userId);
        Counselor counselor = counselorMapper.selectOne(counselorWrapper);

        if (counselor == null) {
            return Result.error("当前用户不是咨询师");
        }

        Long counselorId = counselor.getId();
        var result = appointmentService.getCounselorSchedule(date, page, size, counselorId);
        return Result.success(result);
    }
}