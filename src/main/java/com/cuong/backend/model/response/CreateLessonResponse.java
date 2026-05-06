package com.cuong.backend.model.response;

import lombok.Builder;
import lombok.Data;

/**
 * Response sau khi Admin tạo hoặc upload bài học thành công.
 */
@Data
@Builder
public class CreateLessonResponse {

    /** ID bài học vừa được tạo */
    private int id;

    /** Tên bài học */
    private String lessonName;

    /** Tên chương chứa bài học */
    private String chapterName;

    /** URL video bài giảng đã lưu */
    private String videoUrl;

    /** URL tài liệu PDF đã lưu */
    private String pdfUrl;

    /** Thông báo kết quả */
    private String message;
}
