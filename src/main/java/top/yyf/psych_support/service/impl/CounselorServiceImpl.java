// 文件路径: top.yyf.psych_support.service.impl.CounselorServiceImpl.java
package top.yyf.psych_support.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import top.yyf.psych_support.entity.Appointment;
import top.yyf.psych_support.entity.AppointmentSlot;
import top.yyf.psych_support.entity.Counselor;
import top.yyf.psych_support.mapper.AppointmentMapper;
import top.yyf.psych_support.mapper.AppointmentSlotMapper;
import top.yyf.psych_support.mapper.CounselorMapper;
import top.yyf.psych_support.model.vo.AvailableSlotVO;
import top.yyf.psych_support.model.vo.CounselorDetailVO;
import top.yyf.psych_support.service.ICounselorService;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CounselorServiceImpl implements ICounselorService {

    private final CounselorMapper counselorMapper;
    private final AppointmentSlotMapper appointmentSlotMapper;
    private final AppointmentMapper appointmentMapper;

    @Override
    public List<Counselor> getAllAvailableCounselors() {
        return counselorMapper.selectAllAvailable();
    }

    @Override
    public CounselorDetailVO getCounselorDetail(Long id) {
        Counselor counselor = counselorMapper.selectById(id);
        if (counselor == null || !counselor.getIsAvailable()) {
            throw new RuntimeException("咨询师不存在或已离职");
        }
        return new CounselorDetailVO(counselor);
    }

    @Override
    public List<AvailableSlotVO> getAvailableSlots(Long counselorId, LocalDate date) {
        // 1. 获取该咨询师的所有时间段模板
        List<AppointmentSlot> timeSlots = appointmentSlotMapper.selectList(
                new LambdaQueryWrapper<AppointmentSlot>()
                        .eq(AppointmentSlot::getCounselorId, counselorId)
                        .eq(AppointmentSlot::getStatus, 1)  // 可用的时间段模板
        );

        if (timeSlots.isEmpty()) {
            return new ArrayList<>();
        }

        // 2. 获取该日期已被预约的时间段ID
        List<Appointment> bookedAppointments = appointmentMapper.selectList(
                new LambdaQueryWrapper<Appointment>()
                        .eq(Appointment::getCounselorId, counselorId)
                        .eq(Appointment::getDate, date)
                        .in(Appointment::getStatus, List.of("PENDING", "CONFIRMED"))
        );

        Set<Long> bookedSlotIds = bookedAppointments.stream()
                .map(Appointment::getSlotId)
                .collect(Collectors.toSet());

        // 3. 获取咨询师名称
        Counselor counselor = counselorMapper.selectById(counselorId);
        String counselorName = counselor != null ? counselor.getName() : "";

        // 4. 组装返回结果
        List<AvailableSlotVO> slots = new ArrayList<>();
        for (AppointmentSlot slot : timeSlots) {
            AvailableSlotVO vo = new AvailableSlotVO();
            vo.setSlotId(slot.getId());
            vo.setCounselorId(counselorId);
            vo.setCounselorName(counselorName);
            vo.setDate(date);
            vo.setStartTime(slot.getStartTime());
            vo.setEndTime(slot.getEndTime());
            vo.setIsAvailable(!bookedSlotIds.contains(slot.getId()));
            slots.add(vo);
        }

        return slots;
    }

    @Override
    public Counselor getByUserId(Long userId) {
        return counselorMapper.selectByUserId(userId);
    }
}