package com.xiaomi.hera.trace.etl.manager.config;

import com.xiaomi.mone.es.EsClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author dingpei
 */
@Configuration
@Slf4j
public class EsConfig {

    @Value("${es.trace.address}")
    private String traceAddress;
    @Value("${es.trace.username}")
    private String traceUserName;
    @Value("${es.trace.password}")
    private String tracePassword;

    @Bean
    public EsClient getEsClient() {
        try {
            EsClient esClient = new EsClient(traceAddress, traceUserName, tracePassword);
            log.info("init jaeger es");
            return esClient;
        } catch (Exception e) {
            log.error("init es error : ", e);
        }
        return null;
    }

}
