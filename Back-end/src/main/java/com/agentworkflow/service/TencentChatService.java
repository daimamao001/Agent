package com.agentworkflow.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class TencentChatService {

    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    @Value("${tencent.api-key:}")
    private String apiKey;

    @Value("${tencent.api-base:https://api.hunyuan.cloud.tencent.com/v1}")
    private String apiBase;

    @Value("${tencent.model:hunyuan-lite}")
    private String defaultModel;

    @Value("${tencent.max-tokens:2048}")
    private Integer maxTokens;

    @Value("${tencent.temperature:0.7}")
    private Double temperature;

    public TencentChatService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(15))
                .build();
    }

    public String chat(List<ChatMessage> messages, String modelOverride) throws Exception {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("Tencent API key is missing. Set tencent.api-key or env TENCENT_API_KEY.");
        }
        if (messages == null || messages.isEmpty()) {
            throw new IllegalArgumentException("messages cannot be empty");
        }

        String model = (modelOverride != null && !modelOverride.isBlank()) ? modelOverride : defaultModel;

        Map<String, Object> payload = new HashMap<>();
        payload.put("model", model);
        payload.put("messages", messages);
        if (maxTokens != null) {
            payload.put("max_tokens", maxTokens);
        }
        if (temperature != null) {
            payload.put("temperature", temperature);
        }

        String body = objectMapper.writeValueAsString(payload);
        String endpoint = buildEndpoint("/v1/chat/completions");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(endpoint))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apiKey)
            .timeout(Duration.ofSeconds(60))
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() >= 300) {
            throw new IllegalStateException("Tencent API error: HTTP " + response.statusCode() + " - " + response.body());
        }

        JsonNode root = objectMapper.readTree(response.body());
        JsonNode choices = root.path("choices");
        if (choices.isArray() && choices.size() > 0) {
            JsonNode first = choices.get(0);
            // 标准 OpenAI 兼容字段
            String content = first.path("message").path("content").asText(null);
            if (content != null && !content.isEmpty()) {
                return content;
            }
            // 某些实现可能返回 messages 数组
            if (first.has("messages") && first.path("messages").isArray() && first.path("messages").size() > 0) {
                String alt = first.path("messages").get(0).path("content").asText(null);
                if (alt != null && !alt.isEmpty()) {
                    return alt;
                }
            }
            // delta.content（流式增量的合并场景）
            String delta = first.path("delta").path("content").asText(null);
            if (delta != null && !delta.isEmpty()) {
                return delta;
            }
        }

        // fallback: try top-level content or return full JSON
        if (root.has("content")) {
            return root.get("content").asText();
        }
        return response.body();
    }

    private String buildEndpoint(String path) {
        String base = apiBase;
        if (base.endsWith("/")) {
            base = base.substring(0, base.length() - 1);
        }
        return base + path;
    }

    public record ChatMessage(String role, String content) {}
}
