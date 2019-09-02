package com.canal.demo.model;

import com.alibaba.otter.canal.protocol.CanalEntry;
import lombok.Data;

/**
 * @author answerforever
 * @date 2019/9/2 16:16
 */
@Data
public class OperationType {
    private CanalEntry.EventType eventType;

    private String tableName;
}
