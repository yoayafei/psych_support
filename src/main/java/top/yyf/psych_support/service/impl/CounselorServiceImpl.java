// 文件路径: top.yyf.psych_support.service.impl.CounselorServiceImpl.java
package top.yyf.psych_support.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import top.yyf.psych_support.entity.AppointmentSlot;
import top.yyf.psych_support.entity.Counselor;
import top.yyf.psych_support.mapper.AppointmentSlotMapper;
import top.yyf.psych_support.mapper.CounselorMapper;
import top.yyf.psych_support.model.vo.AvailableSlotVO;
import top.yyf.psych_support.model.vo.CounselorDetailVO;
import top.yyf.psych_support.service.ICounselorService;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CounselorServiceImpl implements ICounselorService {

    private final CounselorMapper counselorMapper;
    private final AppointmentSlotMapper appointmentSlotMapper;

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
    public List<AvailableSlotVO> getAvailableSlots(Long counselorId) { // 移除 date 参数
        // 查询该咨询师的所有可用时间段
        QueryWrapper<AppointmentSlot> slotWrapper = new QueryWrapper<>();
        slotWrapper.eq("counselor_id", counselorId)
                .eq("status", 1); // 只查询状态为AVAILABLE的时间段

        List<AppointmentSlot> slots = appointmentSlotMapper.selectList(slotWrapper);

        return slots.stream().map(slot -> {
            AvailableSlotVO vo = new AvailableSlotVO();
            vo.setId(slot.getId());
            // 注意：这里我们暂时不设置具体日期，因为这是通用时间段
            // 在前端可能需要特殊处理，或者设置一个虚拟日期
            vo.setStartTime(slot.getStartTime().toString());
            vo.setEndTime(slot.getEndTime().toString());
            vo.setIsAvailable(true); // 由于我们只查询AVAILABLE状态，所以默认为true

            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public Counselor getByUserId(Long userId) {
        return counselorMapper.selectByUserId(userId);
    }
}