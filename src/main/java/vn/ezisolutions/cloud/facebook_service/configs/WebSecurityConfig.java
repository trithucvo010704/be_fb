package vn.ezisolutions.cloud.facebook_service.configs;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import vn.ezisolutions.cloud.facebook_service.core.filters.ApiTokenFilter;
import vn.ezisolutions.cloud.facebook_service.core.filters.PrivateApiTokenFilter;

import java.util.List;

@Configuration
public class WebSecurityConfig {


    @Value("${app.allow.origins}")
    private String allowOrigins;

    @Bean
    @Order(1)
    public SecurityFilterChain apiFilterChain(HttpSecurity http, ApiTokenFilter apiTokenFilter) {
        http
                .securityMatcher("/api/**", "/privacy-policy", "/data-deletion")
                .authorizeHttpRequests(authorize -> authorize.anyRequest().permitAll())
                .csrf(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .addFilterBefore(apiTokenFilter, AnonymousAuthenticationFilter.class)
                .cors(Customizer.withDefaults());
        return http.build();
    }

    @Bean
    @Order(2)
    public SecurityFilterChain filterChain(HttpSecurity http, ApiTokenFilter apiTokenFilter) {
        http
                .securityMatcher("/base/**")
                .authorizeHttpRequests(
                        authorize -> authorize.anyRequest().permitAll()
                )
                .csrf(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .addFilterBefore(apiTokenFilter, AnonymousAuthenticationFilter.class)
                .cors(Customizer.withDefaults());
        return http.build();
    }

    @Bean
    @Order(3)
    public SecurityFilterChain bizFilterChain(HttpSecurity http, ApiTokenFilter apiTokenFilter) {
        http
                .securityMatcher("/biz/**")
                .authorizeHttpRequests(authorize -> authorize.anyRequest().permitAll())
                .csrf(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .addFilterBefore(apiTokenFilter, AnonymousAuthenticationFilter.class)
                .cors(Customizer.withDefaults());
        return http.build();
    }

    @Bean
    @Order(4)
    public SecurityFilterChain privateFilterChain(HttpSecurity http, PrivateApiTokenFilter apiTokenFilter) {
        http
                .securityMatcher("/_private/**", "/api/v1/private/**")
                .authorizeHttpRequests(
                        authorize -> authorize.anyRequest().permitAll()
                )
                .csrf(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .addFilterBefore(apiTokenFilter, AnonymousAuthenticationFilter.class)
                .cors(Customizer.withDefaults());
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(List.of(allowOrigins.split(",")));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }


}
