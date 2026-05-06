package com.cuong.backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * Service xử lý upload file vào thư mục local.
 * File được lưu vào thư mục cấu hình trong application.yaml (app.upload-dir).
 * URL trả về dạng: /uploads/{subDir}/{filename}
 * Để truy cập file qua HTTP, cần cấu hình resource handler trong WebConfig.
 */
@Service
public class FileStorageService {

    @Value("${app.upload-dir:uploads}")
    private String uploadDir;

    // Các định dạng video được cho phép
    private static final List<String> ALLOWED_VIDEO_TYPES = Arrays.asList(
            "video/mp4", "video/webm", "video/ogg", "video/quicktime", "video/x-msvideo");

    // Các định dạng PDF được cho phép
    private static final List<String> ALLOWED_PDF_TYPES = Arrays.asList(
            "application/pdf");

    private static final long MAX_VIDEO_SIZE = 500 * 1024 * 1024L; // 500 MB
    private static final long MAX_PDF_SIZE   = 50  * 1024 * 1024L; // 50 MB

    /**
     * Upload video bài giảng.
     *
     * @param file MultipartFile chứa video
     * @return URL tương đối để truy cập file, vd: /uploads/videos/abc123.mp4
     */
    public String uploadVideo(MultipartFile file) throws IOException {
        validateFile(file, ALLOWED_VIDEO_TYPES, MAX_VIDEO_SIZE, "video");
        return saveFile(file, "videos");
    }

    /**
     * Upload tài liệu PDF.
     *
     * @param file MultipartFile chứa PDF
     * @return URL tương đối để truy cập file, vd: /uploads/pdfs/abc123.pdf
     */
    public String uploadPdf(MultipartFile file) throws IOException {
        validateFile(file, ALLOWED_PDF_TYPES, MAX_PDF_SIZE, "PDF");
        return saveFile(file, "pdfs");
    }

    // ===================== PRIVATE HELPERS =====================

    /**
     * Lưu file vào thư mục con subDir bên trong uploadDir.
     * Tên file được đổi thành UUID để tránh xung đột.
     */
    private String saveFile(MultipartFile file, String subDir) throws IOException {
        // Xác định thư mục lưu trữ
        Path targetDir = Paths.get(uploadDir, subDir);
        Files.createDirectories(targetDir);

        // Tạo tên file duy nhất, giữ phần mở rộng gốc
        String originalName = file.getOriginalFilename();
        String extension    = getExtension(originalName);
        String uniqueName   = UUID.randomUUID().toString() + extension;

        // Lưu file
        Path targetPath = targetDir.resolve(uniqueName);
        Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

        // Trả về URL tương đối
        return "/uploads/" + subDir + "/" + uniqueName;
    }

    /**
     * Validate content-type và kích thước file.
     */
    private void validateFile(MultipartFile file, List<String> allowedTypes,
                              long maxSize, String label) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File " + label + " không được để trống.");
        }
        String contentType = file.getContentType();
        if (contentType == null || !allowedTypes.contains(contentType)) {
            throw new IllegalArgumentException(
                    "Định dạng file " + label + " không hợp lệ. Chấp nhận: " + allowedTypes);
        }
        if (file.getSize() > maxSize) {
            throw new IllegalArgumentException(
                    "Kích thước file " + label + " vượt quá giới hạn cho phép (" + (maxSize / 1024 / 1024) + " MB).");
        }
    }

    /** Lấy phần mở rộng file (bao gồm dấu chấm), ví dụ ".mp4" */
    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) return "";
        return filename.substring(filename.lastIndexOf('.'));
    }
}
