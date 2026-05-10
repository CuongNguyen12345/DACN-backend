package com.cuong.backend.util;

public class FormatUtil {
    public static String mapSubjectToDb(String subject) {
        if (subject == null) return null;
        String s = subject.trim().toLowerCase();
        if (s.contains("vật lý") || s.equals("lý")) return "Lý";
        if (s.contains("hóa học") || s.equals("hóa")) return "Hóa";
        if (s.contains("tiếng anh") || s.equals("anh")) return "Anh";
        if (s.contains("toán học") || s.equals("toán")) return "Toán";
        return subject;
    }

    public static String mapGradeToDb(String grade) {
        if (grade == null) return null;
        if (grade.startsWith("Lớp ")) {
            return grade.replace("Lớp ", "").trim();
        }
        return grade;
    }

    public static String mapLevelToDb(String level) {
        if (level == null) return null;
        String l = level.trim().toLowerCase();
        if (l.contains("trung bình"))
            return "MEDIUM";
        else if (l.contains("khó"))
            return "HARD";
        return "BASIC";
    }
}
