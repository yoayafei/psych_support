// 文件路径: top.yyf.psych_support.service.impl.AppointmentServiceImpl.java
package top.yyf.psych_support.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.yyf.psych_support.entity.Appointment;
import top.yyf.psych_support.entity.AppointmentSlot;
import top.yyf.psych_support.entity.Counselor;
import top.yyf.psych_support.entity.User;
import top.yyf.psych_support.exception.BusinessException;
import top.yyf.psych_support.mapper.AppointmentMapper;
import top.yyf.psych_support.mapper.AppointmentSlotMapper;
import top.yyf.psych_support.mapper.CounselorMapper;
import top.yyf.psych_support.mapper.UserMapper;
import top.yyf.psych_support.model.dto.CreateAppointmentDTO;
import top.yyf.psych_support.model.dto.RejectAppointmentDTO;
import top.yyf.psych_support.model.vo.AppointmentVO;
import top.yyf.psych_support.service.IAppointmentService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
@Service
@RequiredArgsConstructor
public class AppointmentServiceImpl extends ServiceImpl<AppointmentMapper, Appointment> implements IAppointmentService {

    private final AppointmentMapper appointmentMapper;
    private final AppointmentSlotMapper appointmentSlotMapper;
    private final CounselorMapper counselorMapper;
    private final UserMapper userMapper;

    // 用于防止并发预约同一个时间段
    private final Lock lock = new ReentrantLock();

    @Override
    @Transactional
    public AppointmentVO createAppointment(CreateAppointmentDTO dto, Long userId) {
        // 1. 参数校验
        if (dto.getCounselorId() == null || dto.getSlotId() == null || dto.getDate() == null) {
            throw new BusinessException("参数错误：缺少必要字段");
        }

        // 2. 检查咨询师是否存在且可用
        Counselor counselor = counselorMapper.selectById(dto.getCounselorId());
        if (counselor == null || !counselor.getIsAvailable()) {
            throw new BusinessException("该咨询师不存在或已离职");
        }

        // 3. 检查时间段模板是否存在且可用
        AppointmentSlot slot = appointmentSlotMapper.selectById(dto.getSlotId());
        if (slot == null || slot.getStatus() != 1) {
            throw new BusinessException("该时间段模板不可用");
        }

        // 4. 检查时间段是否属于该咨询师
        if (!slot.getCounselorId().equals(dto.getCounselorId())) {
            throw new BusinessException("时间段不属于该咨询师");
        }

        // 5. 检查预约日期是否过期（不能预约过去的时间）
        LocalDate today = LocalDate.now();
        if (dto.getDate().isBefore(today)) {
            throw new BusinessException("不能预约过去的日期");
        }

        // 如果是今天，检查时间是否已过
        if (dto.getDate().equals(today)) {
            LocalDateTime slotDateTime = LocalDateTime.of(dto.getDate(), slot.getStartTime());
            if (slotDateTime.isBefore(LocalDateTime.now().plusHours(1))) {
                throw new BusinessException("该时间段已过期，请至少提前1小时预约");
            }
        }

        // 6. 检查用户是否存在
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 7. 检查该时间段在该日期是否已被预约
        lock.lock();
        try {
            // 检查该时间段在该日期是否已有预约
            int bookedCount = appointmentMapper.countByCounselorDateAndSlot(
                    dto.getCounselorId(), dto.getDate(), dto.getSlotId());

            if (bookedCount >= slot.getMaxAppointments()) {
                throw new BusinessException("该时间段已被预约满，请选择其他时间");
            }

            // 检查用户是否已在该时间段有预约
            int userSlotCount = appointmentMapper.countByUserAndSlotAndDate(
                    userId, dto.getSlotId(), dto.getDate());
            if (userSlotCount > 0) {
                throw new BusinessException("您已在此时间段有预约，请勿重复提交");
            }

        } finally {
            lock.unlock();
        }

        // 8. 创建预约记录
        Appointment appointment = new Appointment();
        appointment.setUserId(userId);
        appointment.setCounselorId(dto.getCounselorId());
        appointment.setSlotId(dto.getSlotId());
        appointment.setDate(dto.getDate());  // ✅ 使用前端传来的日期
        appointment.setStartTime(slot.getStartTime());
        appointment.setEndTime(slot.getEndTime());
        appointment.setStatus("PENDING");
        appointment.setReasonForAppointment(dto.getReason());
        appointment.setNotes(dto.getNotes());

        appointmentMapper.insert(appointment);

        log.info("用户 {} 成功创建预约申请，预约ID: {}, 日期: {}", userId, appointment.getId(), dto.getDate());

        return getAppointmentVO(appointment, user, counselor);
    }

    @Override
    public IPage<AppointmentVO> getMyAppointments(Integer page, Integer size, Long userId, String status) {
        Page<Appointment> mpPage = new Page<>(page, size);

        // ✅ 构建查询条件
        LambdaQueryWrapper<Appointment> wrapper = new LambdaQueryWrapper<Appointment>()
                .eq(Appointment::getUserId, userId)
                .orderByDesc(Appointment::getCreatedAt);

        // ✅ 如果传入了 status 参数，则按状态过滤
        if (status != null && !status.isEmpty()) {
            wrapper.eq(Appointment::getStatus, status);
        }

        IPage<Appointment> result = appointmentMapper.selectPage(mpPage, wrapper);

        return result.convert(app -> {
            User user = userMapper.selectById(app.getUserId());
            Counselor counselor = counselorMapper.selectById(app.getCounselorId());
            return getAppointmentVO(app, user, counselor);
        });
    }

    @Override
    public IPage<AppointmentVO> getCounselorAppointments(Integer page, Integer size, Long counselorId, String status) {
        Page<Appointment> mpPage = new Page<>(page, size);

        // ✅ 构建查询条件
        LambdaQueryWrapper<Appointment> wrapper = new LambdaQueryWrapper<Appointment>()
                .eq(Appointment::getCounselorId, counselorId)
                .orderByDesc(Appointment::getCreatedAt);

        // ✅ 如果传入了 status 参数，则按状态过滤
        if (status != null && !status.isEmpty()) {
            wrapper.eq(Appointment::getStatus, status);
        }

        IPage<Appointment> result = appointmentMapper.selectPage(mpPage, wrapper);

        return result.convert(app -> {
            User user = userMapper.selectById(app.getUserId());
            Counselor counselor = counselorMapper.selectById(app.getCounselorId());
            return getAppointmentVO(app, user, counselor);
        });
    }

    @Override
    @Transactional
    public String cancelAppointmentByUser(Long appointmentId, Long userId) {
        Appointment appointment = appointmentMapper.selectById(appointmentId);
        if (appointment == null) {
            throw new BusinessException("预约记录不存在");
        }

        if (!appointment.getUserId().equals(userId)) {
            throw new BusinessException("无权操作此预约");
        }

        if (!"PENDING".equals(appointment.getStatus()) && !"CONFIRMED".equals(appointment.getStatus())) {
            throw new BusinessException("该预约状态无法取消");
        }

        // 检查是否在可取消时间内（比如咨询开始前2小时）
        LocalDateTime slotDateTime = LocalDateTime.of(appointment.getDate(), appointment.getStartTime());
        if (slotDateTime.isBefore(LocalDateTime.now().plusHours(2))) {
            throw new BusinessException("咨询即将开始，无法取消预约");
        }

        // 更新预约状态
        LambdaUpdateWrapper<Appointment> updateWrapper = new LambdaUpdateWrapper<Appointment>()
                .eq(Appointment::getId, appointmentId)
                .in(Appointment::getStatus, "PENDING", "CONFIRMED") // 确保状态正确
                .set(Appointment::getStatus, "CANCELLED_BY_USER")
                .set(Appointment::getUpdatedAt, LocalDateTime.now());

        int rows = appointmentMapper.update(updateWrapper);
        if (rows == 0) {
            throw new BusinessException("取消失败，请稍后再试");
        }

        // 释放时间段（可选，取决于业务规则）
        appointmentSlotMapper.update(null, new LambdaUpdateWrapper<AppointmentSlot>()
                .eq(AppointmentSlot::getId, appointment.getSlotId())
                .set(AppointmentSlot::getStatus, "1"));

        log.info("用户 {} 成功取消预约，预约ID: {}", userId, appointmentId);
        return "预约已取消";
    }

    @Override
    @Transactional
    public String confirmAppointment(Long appointmentId) {
        Appointment appointment = appointmentMapper.selectById(appointmentId);
        if (appointment == null) {
            throw new BusinessException("预约记录不存在");
        }

        if (!"PENDING".equals(appointment.getStatus())) {
            throw new BusinessException("该预约无法确认，状态异常");
        }

        // 更新预约状态
        LambdaUpdateWrapper<Appointment> updateWrapper = new LambdaUpdateWrapper<Appointment>()
                .eq(Appointment::getId, appointmentId)
                .eq(Appointment::getStatus, "PENDING")
                .set(Appointment::getStatus, "CONFIRMED")
                .set(Appointment::getConfirmedAt, LocalDateTime.now())
                .set(Appointment::getUpdatedAt, LocalDateTime.now());

        int rows = appointmentMapper.update(updateWrapper);
        if (rows == 0) {
            throw new BusinessException("确认失败，请稍后再试");
        }

        log.info("预约已确认，ID: {}", appointmentId);
        return "预约已确认";
    }

    @Override
    @Transactional
    public String rejectAppointment(Long appointmentId, RejectAppointmentDTO dto) {
        Appointment appointment = appointmentMapper.selectById(appointmentId);
        if (appointment == null) {
            throw new BusinessException("预约记录不存在");
        }

        if (!"PENDING".equals(appointment.getStatus())) {
            throw new BusinessException("该预约无法拒绝，状态异常");
        }

        // 更新预约状态和备注
        LambdaUpdateWrapper<Appointment> updateWrapper = new LambdaUpdateWrapper<Appointment>()
                .eq(Appointment::getId, appointmentId)
                .eq(Appointment::getStatus, "PENDING")
                .set(Appointment::getStatus, "REJECTED")
                .set(Appointment::getNotes, dto.getRejectionNote()) // 更新备注为拒绝理由
                .set(Appointment::getUpdatedAt, LocalDateTime.now());

        int rows = appointmentMapper.update(updateWrapper);
        if (rows == 0) {
            throw new BusinessException("拒绝失败，请稍后再试");
        }

        // 释放时间段
        appointmentSlotMapper.update(null, new LambdaUpdateWrapper<AppointmentSlot>()
                .eq(AppointmentSlot::getId, appointment.getSlotId())
                .set(AppointmentSlot::getStatus, "1"));

        log.info("预约已拒绝，ID: {}, 原因: {}", appointmentId, dto.getRejectionNote());
        return "预约已拒绝";
    }

    @Override
    @Transactional
    public String completeAppointment(Long appointmentId) {
        Appointment appointment = appointmentMapper.selectById(appointmentId);
        if (appointment == null) {
            throw new BusinessException("预约记录不存在");
        }

        if (!"CONFIRMED".equals(appointment.getStatus())) {
            throw new BusinessException("该预约无法完成，状态异常");
        }

        // 更新预约状态
        LambdaUpdateWrapper<Appointment> updateWrapper = new LambdaUpdateWrapper<Appointment>()
                .eq(Appointment::getId, appointmentId)
                .eq(Appointment::getStatus, "CONFIRMED")
                .set(Appointment::getStatus, "COMPLETED")
                .set(Appointment::getCompletedAt, LocalDateTime.now())
                .set(Appointment::getUpdatedAt, LocalDateTime.now());

        int rows = appointmentMapper.update(updateWrapper);
        if (rows == 0) {
            throw new BusinessException("完成失败，请稍后再试");
        }

        log.info("预约已完成，ID: {}", appointmentId);
        return "预约已完成";
    }

    @Override
    public IPage<AppointmentVO> getCounselorSchedule(LocalDate date, Integer page, Integer size, Long counselorId) {
        Page<Appointment> mpPage = new Page<>(page, size);
        LambdaQueryWrapper<Appointment> wrapper = new LambdaQueryWrapper<Appointment>()
                .eq(Appointment::getCounselorId, counselorId)
                .eq(Appointment::getDate, date)
                .in(Appointment::getStatus, "CONFIRMED", "COMPLETED")
                .orderByAsc(Appointment::getStartTime);

        IPage<Appointment> result = appointmentMapper.selectPage(mpPage, wrapper);

        return result.convert(app -> {
            User user = userMapper.selectById(app.getUserId());
            return getAppointmentVO(app, user, null); // counselor 已知
        });
    }

    @Override
    public AppointmentVO getAppointmentDetail(Long appointmentId, Long currentUserId) {
        Appointment appointment = appointmentMapper.selectById(appointmentId);
        if (appointment == null) {
            throw new BusinessException("预约记录不存在");
        }

        System.out.println("当前用户ID: " + currentUserId);
        System.out.println("预约用户ID: " + appointment.getUserId());
        System.out.println("预约咨询师ID: " + appointment.getCounselorId());

        // ✅ 检查是否是预约用户本人
        boolean isOwner = appointment.getUserId().equals(currentUserId);

        // ✅ 检查是否是关联的咨询师（需要先查询咨询师信息）
        boolean isCounselor = false;
        if (!isOwner) {
            // 根据当前用户ID查询咨询师信息
            LambdaQueryWrapper<Counselor> counselorWrapper = new LambdaQueryWrapper<Counselor>()
                    .eq(Counselor::getUserId, currentUserId);
            Counselor currentCounselor = counselorMapper.selectOne(counselorWrapper);

            if (currentCounselor != null) {
                // 比较咨询师ID
                isCounselor = appointment.getCounselorId().equals(currentCounselor.getId());
            }
        }

        // 权限检查：必须是预约用户或相关咨询师
        if (!isOwner && !isCounselor) {
            throw new BusinessException("无权查看此预约");
        }

        User user = userMapper.selectById(appointment.getUserId());
        Counselor counselor = counselorMapper.selectById(appointment.getCounselorId());
        return getAppointmentVO(appointment, user, counselor);
    }

    // 辅助方法：将实体转换为VO
    // 辅助方法：将实体转换为VO
    private AppointmentVO getAppointmentVO(Appointment app, User user, Counselor counselor) {
        AppointmentVO vo = new AppointmentVO();
        BeanUtils.copyProperties(app, vo);

        // ✅ 设置用户信息
        if (user != null) {
            vo.setUserName(user.getNickname());  // 设置用户昵称
            vo.setUserId(user.getId());          // 设置用户ID
        }

        // ✅ 设置咨询师信息
        if (counselor != null) {
            vo.setCounselorName(counselor.getName());
            vo.setCounselorTitle(counselor.getTitle());
        }

        return vo;
    }
}