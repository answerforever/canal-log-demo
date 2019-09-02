package com.canal.demo.runner;

import com.canal.demo.service.OperationLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * @author answerforever
 * @date 2019/8/31 16:16
 */
@Component
public class ApplicationRunnerImpl implements ApplicationRunner {

    @Autowired
    private OperationLogService operationLogService;

    /**
     * 会在服务启动完成后立即执行
     */
    @Override
    public void run(ApplicationArguments args) throws Exception {

        operationLogService.saveLog();
    }

}
