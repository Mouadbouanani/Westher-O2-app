package esi.ma.backend.config;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;
import java.time.Duration;

@Configuration
public class RestTemplateConfig {

    /**
     * Default RestTemplate with timeouts
     */
    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder.requestFactory(() -> new BufferingClientHttpRequestFactory(
                        createClientFactory(10000, 30000)
                ))
                .build();
    }

    /**
     * RestTemplate for ML service with longer timeout
     */
    @Bean("mlRestTemplate")
    public RestTemplate mlRestTemplate(RestTemplateBuilder builder) {
        return builder.requestFactory(() -> new BufferingClientHttpRequestFactory(
                        createClientFactory(10000, 60000)
                ))
                .build();
    }

    private SimpleClientHttpRequestFactory createClientFactory(int connectTimeout, int readTimeout) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(connectTimeout);
        factory.setReadTimeout(readTimeout);
        return factory;
    }
}