package vn.ezisolutions.cloud.facebook_service.configs;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.ForwardedHeaderFilter;

@Configuration
public class ProxyConfig {

    /**
     * Enables the use of X-Forwarded-* headers for proper URL reconstruction behind a reverse proxy.
     */
    @Bean
    public ForwardedHeaderFilter forwardedHeaderFilter() {
        return new ForwardedHeaderFilter();
    }
}