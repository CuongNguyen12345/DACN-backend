package com.cuong.backend.model.request;

import lombok.Data;

/**
 * Request body để Admin tạo bài học mới với URL video/PDF đã có sẵn.
 * Dùng cho endpoint: POST /api/admin/lessons
 */
@Data
public class CreateLessonRequest {

    /**
     * ID của chapter mà bài học thuộc về.
     * Bắt buộc phải có.
     */
    private int chapterId;

    /**
     * Tên bài học.
     */
    private String lessonName;

    /**
     * Nội dung mô tả / giới thiệu bài học (HTML hoặc plain text).
     */
    private String content;

    /**
     * URL video bài giảng (YouTube embed URL, Google Drive, Cloudinary...).
     * Ví dụ: "https://www.youtube.com/embed/abc123"
     */
    private String videoUrl;

    /**
     * URL tài liệu PDF (Google Drive view link, Cloudinary, S3...).
     * Ví dụ: "https://drive.google.com/file/d/xxx/view"
     */
    private String pdfUrl;

    /**
     * Thời lượng dự kiến.
     */
    private String duration;

    /**
     * Trạng thái bài học (Bản nháp, Đã xuất bản, Đang ẩn).
     */
    private String status;

    /**
     * Định dạng bài học (Video, Lý thuyết, Bài tập).
     */
    private String type;
}
