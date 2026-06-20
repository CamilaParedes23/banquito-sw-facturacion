package com.banquito.switchpagos.billing.client;

import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

@Component
public class CoreKongTokenProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(CoreKongTokenProvider.class);
    private static final Long DEFAULT_EXPIRES_IN_SECONDS = 840L;

    private final RestClient restClient;
    private final String manualAuthToken;
    private final Boolean clientTokenEnabled;
    private final String clientId;
    private final String clientSecret;
    private final String requiredScope;
    private final String clientTokenPath;
    private final Long refreshSkewSeconds;

    private String cachedToken;
    private Instant cachedTokenExpiresAt;

    public CoreKongTokenProvider(
            @Value("${core.kong.base-url}") String baseUrl,
            @Value("${core.kong.auth-token}") String manualAuthToken,
            @Value("${core.kong.client-token.enabled}") Boolean clientTokenEnabled,
            @Value("${core.kong.client-id}") String clientId,
            @Value("${core.kong.client-secret}") String clientSecret,
            @Value("${core.kong.required-scope}") String requiredScope,
            @Value("${core.kong.client-token-path}") String clientTokenPath,
            @Value("${core.kong.client-token-refresh-skew-seconds}") Long refreshSkewSeconds,
            @Value("${core.kong.connect-timeout-ms}") Long connectTimeoutMs,
            @Value("${core.kong.read-timeout-ms}") Long readTimeoutMs) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofMillis(connectTimeoutMs));
        requestFactory.setReadTimeout(Duration.ofMillis(readTimeoutMs));
        this.restClient = RestClient.builder()
                .baseUrl(normalizeBaseUrl(baseUrl))
                .requestFactory(requestFactory)
                .build();
        this.manualAuthToken = manualAuthToken == null ? "" : manualAuthToken.trim();
        this.clientTokenEnabled = clientTokenEnabled;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.requiredScope = requiredScope;
        this.clientTokenPath = normalizePath(clientTokenPath);
        this.refreshSkewSeconds = refreshSkewSeconds == null ? 60L : refreshSkewSeconds;
    }

    public String getBearerToken() {
        if (StringUtils.hasText(manualAuthToken)) {
            return manualAuthToken;
        }
        if (!Boolean.TRUE.equals(clientTokenEnabled)) {
            throw new IllegalStateException("Core client-token automatico deshabilitado y CORE_KONG_AUTH_TOKEN no configurado.");
        }
        if (isCachedTokenValid()) {
            return cachedToken;
        }
        synchronized (this) {
            if (isCachedTokenValid()) {
                return cachedToken;
            }
            acquireToken();
            return cachedToken;
        }
    }

    private Boolean isCachedTokenValid() {
        return StringUtils.hasText(cachedToken)
                && cachedTokenExpiresAt != null
                && Instant.now().plusSeconds(refreshSkewSeconds).isBefore(cachedTokenExpiresAt);
    }

    private void acquireToken() {
        validateClientCredentials();
        try {
            ClientTokenRequest request = new ClientTokenRequest();
            request.setClientId(clientId);
            request.setClientSecret(clientSecret);
            request.setRequiredScope(requiredScope);
            Map<String, Object> response = restClient.post()
                    .uri(clientTokenPath)
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .body(Map.class);
            String token = extractToken(response);
            Long expiresIn = extractExpiresIn(response);
            cachedToken = token;
            cachedTokenExpiresAt = Instant.now().plusSeconds(expiresIn);
            LOGGER.info("Core client-token acquired, expiresIn={}s", expiresIn);
        } catch (RestClientResponseException exception) {
            LOGGER.warn("Core client-token unavailable. httpStatus={}", exception.getStatusCode().value());
            throw new IllegalStateException("No se pudo obtener client-token de Core/Kong.", exception);
        } catch (ResourceAccessException exception) {
            LOGGER.warn("Core client-token unavailable. connectivity={}", exception.getMessage());
            throw new IllegalStateException("No se pudo conectar a Core/Kong para obtener client-token.", exception);
        }
    }

    private void validateClientCredentials() {
        if (!StringUtils.hasText(clientId) || !StringUtils.hasText(clientSecret)) {
            throw new IllegalStateException("CORE_KONG_CLIENT_ID y CORE_KONG_CLIENT_SECRET son requeridos para client-token automatico.");
        }
        if (!StringUtils.hasText(requiredScope)) {
            throw new IllegalStateException("CORE_KONG_REQUIRED_SCOPE es requerido para client-token automatico.");
        }
    }

    private String extractToken(Map<String, Object> response) {
        if (response == null) {
            throw new IllegalStateException("Core client-token no devolvio body.");
        }
        String accessToken = text(response, "accessToken");
        if (StringUtils.hasText(accessToken)) {
            return accessToken;
        }
        String token = text(response, "token");
        if (StringUtils.hasText(token)) {
            return token;
        }
        throw new IllegalStateException("Core client-token no devolvio accessToken/token.");
    }

    private Long extractExpiresIn(Map<String, Object> response) {
        String expiresAt = text(response, "expiresAt");
        if (StringUtils.hasText(expiresAt)) {
            try {
                Long seconds = Duration.between(Instant.now(), OffsetDateTime.parse(expiresAt).toInstant()).toSeconds();
                return Math.max(1L, seconds - refreshSkewSeconds);
            } catch (RuntimeException exception) {
                LOGGER.warn("Core client-token expiresAt invalido; se usa TTL conservador.");
            }
        }
        Object expiresInSecondsValue = response == null ? null : response.get("expiresInSeconds");
        if (expiresInSecondsValue instanceof Number number) {
            return Math.max(1L, number.longValue() - refreshSkewSeconds);
        }
        Object expiresInValue = response == null ? null : response.get("expiresIn");
        if (expiresInValue instanceof Number number) {
            return Math.max(1L, number.longValue() - refreshSkewSeconds);
        }
        return DEFAULT_EXPIRES_IN_SECONDS;
    }

    private String text(Map<String, Object> node, String fieldName) {
        Object value = node == null ? null : node.get(fieldName);
        return value == null ? null : value.toString();
    }

    private String normalizeBaseUrl(String baseUrl) {
        String value = StringUtils.hasText(baseUrl) ? baseUrl.trim() : "http://localhost:8000";
        return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
    }

    private String normalizePath(String path) {
        if (!StringUtils.hasText(path)) {
            return "/api/v1/auth/client-token";
        }
        String value = path.trim();
        return value.startsWith("/") ? value : "/" + value;
    }

    private static class ClientTokenRequest {
        private String clientId;
        private String clientSecret;
        private String requiredScope;

        public String getClientId() {
            return clientId;
        }

        public void setClientId(String clientId) {
            this.clientId = clientId;
        }

        public String getClientSecret() {
            return clientSecret;
        }

        public void setClientSecret(String clientSecret) {
            this.clientSecret = clientSecret;
        }

        public String getRequiredScope() {
            return requiredScope;
        }

        public void setRequiredScope(String requiredScope) {
            this.requiredScope = requiredScope;
        }
    }
}
