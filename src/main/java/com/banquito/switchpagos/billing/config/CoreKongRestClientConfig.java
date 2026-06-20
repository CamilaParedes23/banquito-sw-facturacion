package com.banquito.switchpagos.billing.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.time.Duration;

@Configuration
public class CoreKongRestClientConfig {

    @Bean
    public RestClient coreKongRestClient(
            @Value("${core.kong.base-url}") String baseUrl,
            @Value("${core.kong.connect-timeout-ms}") Integer connectTimeoutMs,
            @Value("${core.kong.read-timeout-ms}") Integer readTimeoutMs) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofMillis(connectTimeoutMs));
        requestFactory.setReadTimeout(Duration.ofMillis(readTimeoutMs));
        return RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(requestFactory)
                .build();
    }
}
