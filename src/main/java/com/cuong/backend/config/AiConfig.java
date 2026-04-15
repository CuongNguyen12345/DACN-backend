package com.cuong.backend.config;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;

@Configuration
public class AiConfig {

    @Value("${gemini.api.key}")
    private String apiKey;

    // Model dành cho Admin (Tạo đề thi, phân loại bài học)
    @Bean
    @Qualifier("adminModel")
    public ChatLanguageModel adminModel() {
        return GoogleAiGeminiChatModel.builder()
                .apiKey(apiKey)
                .modelName("gemma-4-31b-it")
                .temperature(0.2) // Thấp để chính xác
                .build();
    }

    // Model dành cho Chatbot (Giải đáp bài tập cho học sinh)
    @Bean
    @Qualifier("chatModel")
    public ChatLanguageModel chatModel() {
        return GoogleAiGeminiChatModel.builder()
                .apiKey(apiKey)
                .modelName("gemini-3-flash-live") // Model Unlimited RPD
                .temperature(0.7) // Cao hơn một chút để hội thoại tự nhiên
                .build();
    }

}
