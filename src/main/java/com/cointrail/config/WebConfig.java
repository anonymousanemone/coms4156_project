package com.cointrail.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC configuration for the Cointrail API.
 *
 * <p>Enables CORS on the {@code /v1} endpoints so that browser-based
 * client apps (e.g. the Price Tag Generator demo client) served from
 * a different origin can call the API directly.</p>
 */
@Configuration(proxyBeanMethods = false)
public final class WebConfig implements WebMvcConfigurer {

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
}
