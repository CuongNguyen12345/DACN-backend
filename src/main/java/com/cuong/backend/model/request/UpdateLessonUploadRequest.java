package com.cuong.backend.model.request;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.web.multipart.MultipartFile;

@Data
@EqualsAndHashCode(callSuper = true)
public class UpdateLessonUploadRequest extends UpdateLessonRequest {
    private MultipartFile videoFile;
    private MultipartFile pdfFile;
}
