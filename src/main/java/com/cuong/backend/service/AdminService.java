package com.cuong.backend.service;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import dev.langchain4j.model.chat.ChatLanguageModel;

@Service
public class AdminService {

    private final ChatLanguageModel aiModel;

    public AdminService(@Qualifier("adminModel") ChatLanguageModel aiModel) {
        this.aiModel = aiModel;
    }

    public String generateQuiz(String lessonContent) {
        // Gọi AI model để sinh câu hỏi
        return aiModel.generate(lessonContent);
    }
}
