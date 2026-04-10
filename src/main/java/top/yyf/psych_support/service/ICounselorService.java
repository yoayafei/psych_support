// 文件路径: top.yyf.psych_support.service.ICounselorService.java
package top.yyf.psych_support.service;

import top.yyf.psych_support.entity.Counselor;
import top.yyf.psych_support.model.vo.AvailableSlotVO;
import top.yyf.psych_support.model.vo.CounselorDetailVO;

import java.time.LocalDate;
import java.util.List;

public interface ICounselorService {

    /**
     * 获取所有可用咨询师列表
     */
    List<Counselor> getAllAvailableCounselors();

    /**
     * 获取咨询师详情
     */
    CounselorDetailVO getCounselorDetail(Long id);

    /**
     * 获取咨询师某天的可预约时间段
     */
    List<AvailableSlotVO> getAvailableSlots(Long counselorId);

    /**
     * 根据用户ID获取咨询师信息
     */
    Counselor getByUserId(Long userId);
}