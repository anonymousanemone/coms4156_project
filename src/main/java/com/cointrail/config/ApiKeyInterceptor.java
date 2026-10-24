package com.cointrail.config;

import com.cointrail.model.Client;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import tools.jackson.databind.json.JsonMapper;

/**
 * Rejects any request whose API key is not one of the registered clients.
 */
@Component
public final class ApiKeyInterceptor implements HandlerInterceptor {

    /** The request header that carries the API key. */
    public static final String HEADER = "X-API-Key";

    /** Where the registered clients are listed, on the classpath. */
    private static final String CLIENTS_FILE = "data/clients.json";

    /** The API keys that are allowed in. */
    private final Set<String> validKeys;

    /**
     * Constructs the interceptor with the keys in {@code clients.json}.
     */
    public ApiKeyInterceptor() {
        this(loadKeys());
    }

    /**
     * Constructs the interceptor with the given keys.
     *
     * @param validKeys the API keys that are allowed in
     */
    public ApiKeyInterceptor(final Set<String> validKeys) {
        this.validKeys = Set.copyOf(validKeys);
    }

    /**
     * Checks the API key before the request reaches a controller.
     *
     * @param request the request
     * @param response the response
     * @param handler the handler the request is going to
     * @return true if the request may continue
     * @throws UnauthorizedException if the key is missing or unknown
     */
    @Override
    public boolean preHandle(
        final HttpServletRequest request,
        final HttpServletResponse response,
        final Object handler
    ) {
        // CORS preflight requests never carry custom headers.
        if ("OPTIONS".equals(request.getMethod())) {
            return true;
        }
        String key = request.getHeader(HEADER);
        if (key == null || key.isBlank()) {
            throw new UnauthorizedException("Missing " + HEADER + " header");
        }
        if (!validKeys.contains(key)) {
            throw new UnauthorizedException("Unknown API key");
        }
        return true;
    }

    /**
     * Reads the API keys of every client in {@code clients.json}.
     *
     * @return the keys
     */
    private static Set<String> loadKeys() {
        try (InputStream in =
                 new ClassPathResource(CLIENTS_FILE).getInputStream()) {
            Client[] clients =
                JsonMapper.builder().build().readValue(in, Client[].class);
            return Arrays.stream(clients)
                .map(Client::getApiKey)
                .collect(Collectors.toSet());
        } catch (IOException e) {
            throw new UncheckedIOException(
                "Could not read " + CLIENTS_FILE, e
            );
        }
    }
}
