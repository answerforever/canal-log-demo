package com.canal.demo.config;

import com.alibaba.otter.canal.client.CanalConnector;
import com.alibaba.otter.canal.client.CanalConnectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.InetSocketAddress;

/**
 * @author answerforever
 * @date 2019/8/31 15:08
 */
@Configuration
public class CanalConfig {

    // @Value 获取 Apollo配置中端内容
    @Value("${canal.server.ip}")
    private String canalIp;
    @Value("${canal.server.port}")
    private Integer canalPort;
    @Value("${canal.destination}")
    private String destination;
    @Value("{canal.username}")
    private String userName;
    @Value("{canal.password}")
    private String password;

//    @Value("${elasticSearch.server.ip}")
//    private String elasticSearchIp;
//    @Value("${elasticSearch.server.port}")
//    private Integer elasticSearchPort;
//    @Value("${zookeeper.server.ip}")
//    private String zkServerIp;

    /**
     * 获取简单canal-server连接
     */
    @Bean
    public CanalConnector canalSimpleConnector() {
        CanalConnector canalConnector = CanalConnectors.newSingleConnector(new InetSocketAddress(canalIp, canalPort),
                destination, userName, password);
        return canalConnector;
    }

}
