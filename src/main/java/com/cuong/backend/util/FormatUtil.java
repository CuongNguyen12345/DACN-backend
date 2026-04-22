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

    public static String mapSubjectToFe(String dbName) {
        if ("Lý".equals(dbName))
            return "Vật Lý";
        if ("Hóa".equals(dbName))
            return "Hóa Học";
        if ("Anh".equals(dbName))
            return "Tiếng Anh";
        return dbName;
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
        return "EASY";
    }

    public static String mapLevelToFe(String dbLevel) {
        if ("MEDIUM".equals(dbLevel))
            return "Trung bình";
        else if ("HARD".equals(dbLevel))
            return "Khó";
        return "Dễ";
    }
}
