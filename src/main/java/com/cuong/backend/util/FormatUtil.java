package com.cuong.backend.util;

public class FormatUtil {
    public static String mapSubjectToDb(String subject) {
        if ("Vật Lý".equals(subject))
            return "Lý";
        if ("Hóa Học".equals(subject))
            return "Hóa";
        if ("Tiếng Anh".equals(subject))
            return "Anh";
        return subject;
    }

    public static String mapGradeToDb(String grade) {
        if (grade != null && grade.startsWith("Lớp ")) {
            return grade.replace("Lớp ", "").trim();
        }
        return grade;
    }

    public static String mapLevelToDb(String level) {
        if ("Trung bình".equals(level) || "Trung Bình".equals(level))
            return "MEDIUM";
        else if ("Khó".equals(level))
            return "HARD";
        return "BASIC";
    }
}
