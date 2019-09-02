package com.canal.demo.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.otter.canal.client.CanalConnector;
import com.alibaba.otter.canal.protocol.CanalEntry;
import com.alibaba.otter.canal.protocol.Message;
import com.alibaba.otter.canal.protocol.exception.CanalClientException;
import com.canal.demo.constant.MongoDBConstant;
import com.canal.demo.model.OperationType;
import com.canal.demo.model.mongo.OperationLogMongoEntity;
import com.canal.demo.service.OperationLogService;
import com.canal.demo.util.CanalDataParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;

/**
 * @author answerforever
 * @date 2019/8/31 15:18
 */
@Service
@Slf4j
public class OperationLogServiceImpl implements OperationLogService {

    @Autowired
    private CanalConnector canalSimpleConnector;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public void saveLog() {
        try {
            openCanalConnector(canalSimpleConnector);
            // 轮询拉取数据
            Integer batchSize = 5 * 1024;
            while (true) {
                Message message = canalSimpleConnector.getWithoutAck(batchSize);
                long id = message.getId();
                int size = message.getEntries().size();
                log.info("当前监控到binLog消息数量{}", size);
                if (id == -1 || size == 0) {
                    try {
                        // 等待4秒
                        Thread.sleep(4000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } else {
                    //1. 解析message对象
                    List<CanalEntry.Entry> entries = message.getEntries();
                    List<CanalDataParser.ThreeTuple<OperationType, Map, Map>> rows =
                            CanalDataParser.printEntry(entries);

                    for (CanalDataParser.ThreeTuple<OperationType, Map, Map> tuple : rows) {
                        CanalEntry.EventType eventType = tuple.eventType.getEventType();
                        if (eventType == CanalEntry.EventType.INSERT
                                || eventType == CanalEntry.EventType.UPDATE
                                || eventType == CanalEntry.EventType.DELETE) {
                            saveOperationMapData(tuple.eventType, tuple.columnMapBefore, tuple.columnMapAfter);
                        }
                    }
                }
                //消息确认已处理
                canalSimpleConnector.ack(id);
            }
        } catch (CanalClientException e) {
            e.printStackTrace();
        } finally {
            closeCanalConnector(canalSimpleConnector);
        }
    }

    /**
     * 打开canal连接
     *
     * @param canalConnector
     */
    private void openCanalConnector(CanalConnector canalConnector) {
        //连接CanalServer
        canalConnector.connect();
        // 订阅destination
        canalConnector.subscribe("hhotel-cbs-common\\.notify.*");
        canalConnector.rollback();
    }

    /**
     * 关闭canal连接
     *
     * @param canalConnector
     */
    private void closeCanalConnector(CanalConnector canalConnector) {
        //关闭连接CanalServer
        canalConnector.disconnect();
        // 注销订阅destination
//        canalConnector.unsubscribe();
    }

    private OperationLogMongoEntity getOperationLogEntity(OperationType operationType, Map beforeData, Map afterData) {
        String tableName = operationType.getTableName();
        String jsonValueBefore = CollectionUtils.isEmpty(beforeData) ? "" : JSON.toJSONString(beforeData);
        String jsonValueAfter = CollectionUtils.isEmpty(afterData) ? "" : JSON.toJSONString(afterData);
        OperationLogMongoEntity operationLogMongoEntity = new OperationLogMongoEntity();
        operationLogMongoEntity.setActionType(operationType.getEventType().name());
        operationLogMongoEntity.setTableName(tableName);
        operationLogMongoEntity.setAfterValue(jsonValueAfter);
        operationLogMongoEntity.setBeforeValue(jsonValueBefore);
        operationLogMongoEntity.setCreateTime(System.currentTimeMillis());
        return operationLogMongoEntity;
    }


    private void saveLog(List<OperationLogMongoEntity> list) {
        mongoTemplate.insert(list, MongoDBConstant.OPERATION_LOG_INFO);
    }

    private void saveLog(OperationLogMongoEntity entity) {
        mongoTemplate.insert(entity, MongoDBConstant.OPERATION_LOG_INFO);
    }

    private void saveOperationMapData(OperationType operationType, Map beforeData, Map afterData) {
        OperationLogMongoEntity entity = getOperationLogEntity(operationType, beforeData, afterData);
        if (entity != null) {
            saveLog(entity);
        }
    }

}
