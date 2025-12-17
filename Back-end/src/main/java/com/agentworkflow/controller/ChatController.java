package com.agentworkflow.controller;

import com.agentworkflow.service.TencentChatService;
import com.agentworkflow.utils.ApiResponse;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    @Autowired
    private TencentChatService tencentChatService;

    @PostMapping
    public ApiResponse chat(@RequestBody ChatRequest request) {
        try {
            if (request == null || request.getMessages() == null || request.getMessages().isEmpty()) {
                return ApiResponse.fail(400, "messages 不能为空");
            }
            String content = tencentChatService.chat(request.getMessages(), request.getModel());
            return ApiResponse.success(Map.of("content", content));
        } catch (Exception e) {
            return ApiResponse.fail(500, "调用腾讯大模型失败: " + e.getMessage());
        }
    }

    @Data
    public static class ChatRequest {
        private List<TencentChatService.ChatMessage> messages;
        private String model;
    }
}
