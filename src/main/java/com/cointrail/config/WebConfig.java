package com.cointrail.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC configuration for the Cointrail API.
 *
 * <p>Enables CORS on the {@code /v1} endpoints so that browser-based
 * client apps served from a different origin can call the API directly,
 * and requires an API key on every {@code /v1} request.</p>
 */
@Configuration(proxyBeanMethods = false)
public final class WebConfig implements WebMvcConfigurer {

    /** Checks the API key on each request. */
    private final ApiKeyInterceptor apiKeyInterceptor;

    /**
     * Constructs the configuration.
     *
     * @param apiKeyInterceptor checks the API key on each request
     */
    public WebConfig(final ApiKeyInterceptor apiKeyInterceptor) {
        this.apiKeyInterceptor = apiKeyInterceptor;
    }

    /**
     * Adds permissive CORS mappings for the API endpoints.
     *
     * @param registry the CORS registry to configure
     */
    @Override
    public void addCorsMappings(
        final CorsRegistry registry
    ) {
        registry.addMapping("/v1/**")
            .allowedOrigins("*")
            .allowedMethods(
                "GET", "POST", "PATCH", "DELETE"
            );
    }

    /**
     * Requires an API key on every {@code /v1} request.
     *
     * @param registry the interceptor registry to configure
     */
    @Override
    public void addInterceptors(final InterceptorRegistry registry) {
        registry.addInterceptor(apiKeyInterceptor).addPathPatterns("/v1/**");
    }
}
