package vn.ezisolutions.cloud.facebook_service.configs;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;
import vn.ezisolutions.cloud.facebook_service.core.handlers.FacebookResponseErrorHandler;
import vn.ezisolutions.cloud.facebook_service.core.properties.FacebookProperties;

@Configuration
@RequiredArgsConstructor
public class RestTemplateConfig {

    private final FacebookProperties facebookProperties;

    @Bean
    public RestTemplate facebookRestTemplate() {
        String baseUrl = facebookProperties.getGraphHost() + "/" + facebookProperties.getGraphVersion();

        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(5000);  // 5 seconds
        requestFactory.setReadTimeout(30000);    // Facebook may fetch attachment URLs before responding.

        RestTemplate restTemplate = new RestTemplate(requestFactory);
        restTemplate.setUriTemplateHandler(new DefaultUriBuilderFactory(baseUrl));
        restTemplate.setErrorHandler(new FacebookResponseErrorHandler());
        return restTemplate;
    }
}
