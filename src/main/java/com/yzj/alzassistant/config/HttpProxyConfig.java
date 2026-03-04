package com.yzj.alzassistant.config;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * HTTP代理配置类
 * 用于配置HTTP/HTTPS代理，以支持访问被墙的API（如OpenAI API）
 */
@Slf4j
@Configuration
public class HttpProxyConfig {

    @Value("${http.proxy.host:}")
    private String proxyHost;

    @Value("${http.proxy.port:}")
    private String proxyPort;

    @Value("${http.proxy.enabled:false}")
    private boolean proxyEnabled;

    @PostConstruct
    public void configureProxy() {
        if (proxyEnabled && proxyHost != null && !proxyHost.isEmpty() && proxyPort != null && !proxyPort.isEmpty()) {
            System.setProperty("http.proxyHost", proxyHost);
            System.setProperty("http.proxyPort", proxyPort);
            System.setProperty("https.proxyHost", proxyHost);
            System.setProperty("https.proxyPort", proxyPort);
            log.info("HTTP代理已配置: {}:{}", proxyHost, proxyPort);
        } else {
            log.info("HTTP代理未启用或配置不完整");
        }
    }
}






