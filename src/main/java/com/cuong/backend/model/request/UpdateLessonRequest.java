package com.cuong.backend.model.request;

import lombok.Data;

/**
 * Request body để Admin cập nhật bài học với URL video/PDF dạng text.
 * Dùng cho endpoint: PUT /api/admin/lessons/{id}
 */
@Data
public class UpdateLessonRequest {

    /**
     * ID của chapter. Nếu null thì giữ nguyên chapter cũ.
     */
    private Integer chapterId;

    /**
     * Tên bài học. Nếu null hoặc rỗng thì giữ nguyên tên cũ.
     */
    private String lessonName;

    /**
     * Nội dung mô tả / giới thiệu bài học.
     */
    private String content;

    /**
     * URL video bài giảng.
     */
    private String videoUrl;

    /**
     * URL tài liệu PDF.
     */
    private String pdfUrl;

    /**
     * Thời lượng dự kiến.
     */
    private String duration;

    /**
     * Trạng thái bài học.
     */
    private String status;

    /**
     * Định dạng bài học.
     */
    private String type;
}
