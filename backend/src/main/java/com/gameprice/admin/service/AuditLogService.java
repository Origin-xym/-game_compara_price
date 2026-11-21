package com.gameprice.admin.service;

public interface AuditLogService {

    /**
     * 写一条操作留痕。before / after 可为 null，内部做 JSON 序列化。
     */
    void record(String module, String action, Long targetId, String targetName,
                Object before, Object after);
}
