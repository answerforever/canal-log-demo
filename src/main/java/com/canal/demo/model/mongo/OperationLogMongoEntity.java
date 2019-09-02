package com.canal.demo.model.mongo;

import com.canal.demo.constant.MongoDBConstant;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;


/**
 * 操作日志
 *
 * @author answerforever
 * @date 2019/8/31 15:26
 */
@Data
@Document(collection = MongoDBConstant.OPERATION_LOG_INFO)
public class OperationLogMongoEntity {

    /**
     * 表名
     */
    private String tableName;

    /**
     * 操作类型
     */
    private String actionType;

    /**
     * 修改之前
     */
    private String beforeValue;

    /**
     * 修改之后
     */
    private String afterValue;

    /**
     * 创建时间
     */
    private Long createTime;

}
