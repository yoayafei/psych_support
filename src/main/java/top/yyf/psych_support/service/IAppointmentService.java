// 文件路径: top.yyf.psych_support.service.IAppointmentService.java
package top.yyf.psych_support.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import top.yyf.psych_support.model.dto.CreateAppointmentDTO;
import top.yyf.psych_support.model.dto.RejectAppointmentDTO;
import top.yyf.psych_support.model.vo.AppointmentVO;

import java.time.LocalDate;

public interface IAppointmentService {

    /**
     * 创建预约申请
     */
    AppointmentVO createAppointment(CreateAppointmentDTO dto, Long userId);

    /**
     * 获取我的预约记录（分页）
     */
    IPage<AppointmentVO> getMyAppointments(Integer page, Integer size, Long userId, String status);
    /**
     * 获取咨询师的预约记录（分页）
     */
    IPage<AppointmentVO> getCounselorAppointments(Integer page, Integer size, Long counselorId, String status);
    /**
     * 用户取消预约
     */
    String cancelAppointmentByUser(Long appointmentId, Long userId);

    /**
     * 咨询师/管理员确认预约
     */
    String confirmAppointment(Long appointmentId);

    /**
     * 咨询师/管理员拒绝预约
     */
    String rejectAppointment(Long appointmentId, RejectAppointmentDTO dto);

    /**
     * 咨询师标记预约完成
     */
    String completeAppointment(Long appointmentId);

    /**
     * 查询咨询师当天的预约安排
     */
    IPage<AppointmentVO> getCounselorSchedule(LocalDate date, Integer page, Integer size, Long counselorId);

    /**
     * 获取预约详情
     */
    AppointmentVO getAppointmentDetail(Long appointmentId, Long currentUserId);
}