package com.gameprice.alert.service;

import com.gameprice.alert.dto.AlertRuleRequest;
import com.gameprice.alert.vo.AlertRuleVO;

import java.util.List;

public interface AlertService {

    /** 设置提醒（AC-10.1 / AC-10.7） */
    Long create(AlertRuleRequest request);

    /** 我的提醒列表 */
    List<AlertRuleVO> listMine();

    /** 暂停 / 恢复（AC-10.5） */
    void changeStatus(Long id, Integer status);

    /** 删除（AC-10.5） */
    void delete(Long id);
}
