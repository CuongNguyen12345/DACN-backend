package com.cuong.backend.service;

import com.cuong.backend.entity.ChapterEntity;
import com.cuong.backend.entity.LessonEntity;
import com.cuong.backend.entity.SubjectEntity;
import com.cuong.backend.entity.UserProgressEntity;
import com.cuong.backend.model.response.ChapterResponseDTO;
import com.cuong.backend.model.response.LessonResponseDTO;
import com.cuong.backend.model.response.PageResponse;
import com.cuong.backend.repository.ChapterRepository;
import com.cuong.backend.repository.LessonRepository;
import com.cuong.backend.repository.SubjectRepository;
import com.cuong.backend.repository.UserProgressRepository;
import jakarta.persistence.criteria.Predicate;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CourseService {

    private final SubjectRepository subjectRepository;
    private final ChapterRepository chapterRepository;
    private final LessonRepository lessonRepository;
    private final UserProgressRepository userProgressRepository;

    public CourseService(SubjectRepository subjectRepository,
                         ChapterRepository chapterRepository,
                         LessonRepository lessonRepository,
                         UserProgressRepository userProgressRepository) {
        this.subjectRepository = subjectRepository;
        this.chapterRepository = chapterRepository;
        this.lessonRepository = lessonRepository;
        this.userProgressRepository = userProgressRepository;
    }
    
    public PageResponse<ChapterResponseDTO> getCourseData(String grade, String subjectName, String keyword, int page, int size) {
        List<SubjectEntity> subjects;
        
        boolean isAllGrade = grade == null || "all".equals(grade);
        boolean isAllSubject = subjectName == null || "all".equals(subjectName);

        if (!isAllGrade && !isAllSubject) {
            subjects = subjectRepository.findByNameAndGrade(subjectName, grade)
                    .map(List::of)
                    .orElse(List.of());
        } else if (!isAllGrade) {
            subjects = subjectRepository.findByGrade(grade);
        } else if (!isAllSubject) {
            subjects = subjectRepository.findAll().stream()
                    .filter(s -> s.getName().equals(subjectName))
                    .collect(Collectors.toList());
        } else {
            subjects = subjectRepository.findAll();
        }

        List<ChapterResponseDTO> allMatchingChapters = new ArrayList<>();

        for (SubjectEntity sub : subjects) {
            List<ChapterEntity> chapters = chapterRepository.findBySubjectIdOrderByOrderNumberAsc(sub.getId());
            
            for (ChapterEntity chap : chapters) {
                Specification<LessonEntity> spec = (root, query, cb) -> {
                    List<Predicate> predicates = new ArrayList<>();
                    predicates.add(cb.equal(root.get("chapterId"), chap.getId()));
                    if (keyword != null && !keyword.isEmpty()) {
                        predicates.add(cb.like(cb.lower(root.get("lessonName")), "%" + keyword.toLowerCase() + "%"));
                    }
                    return cb.and(predicates.toArray(new Predicate[0]));
                };

                List<LessonEntity> lessons = lessonRepository.findAll(spec);
                
                if (!lessons.isEmpty()) {
                    allMatchingChapters.add(ChapterResponseDTO.builder()
                            .id(chap.getId())
                            .chapterName(chap.getChapterName())
                            .lessons(lessons.stream().map(l -> LessonResponseDTO.builder()
                                    .id(l.getId())
                                    .lessonName(l.getLessonName())
                                    .subjectBadge(sub.getName() + " " + sub.getGrade())
                                    .videoUrl(l.getVideoUrl())
                                    .pdfUrl(l.getPdfUrl())
                                    .build()).collect(Collectors.toList()))
                            .build());
                }
            }
        }

        // Manual Pagination
        int totalElements = allMatchingChapters.size();
        int totalPages = (int) Math.ceil((double) totalElements / size);
        int start = (page - 1) * size;
        int end = Math.min(start + size, totalElements);

        List<ChapterResponseDTO> paginatedData = new ArrayList<>();
        if (start < totalElements) {
            paginatedData = allMatchingChapters.subList(start, end);
        }

        return PageResponse.<ChapterResponseDTO>builder()
                .currentPage(page)
                .totalPages(totalPages)
                .pageSize(size)
                .totalElements(totalElements)
                .data(paginatedData)
                .build();
    }

    public Integer getFirstLessonId(String grade, String subjectName) {
        List<SubjectEntity> subjects = subjectRepository.findByNameAndGrade(subjectName, grade)
                .map(List::of)
                .orElse(List.of());
        
        if (subjects.isEmpty()) return null;
        
        SubjectEntity sub = subjects.get(0);
        List<ChapterEntity> chapters = chapterRepository.findBySubjectIdOrderByOrderNumberAsc(sub.getId());
        
        if (chapters.isEmpty()) return null;
        
        for (ChapterEntity chap : chapters) {
            List<LessonEntity> lessons = lessonRepository.findByChapterId(chap.getId());
            if (!lessons.isEmpty()) {
                return lessons.get(0).getId();
            }
        }
        
        return null;
    }

    public List<ChapterResponseDTO> getCourseDataByLessonId(Integer lessonId) {
        LessonEntity currentLesson = lessonRepository.findById(lessonId).orElse(null);
        if (currentLesson == null) return List.of();
        
        ChapterEntity chapter = chapterRepository.findById(currentLesson.getChapterId()).orElse(null);
        if (chapter == null) return List.of();
        
        SubjectEntity sub = subjectRepository.findById(chapter.getSubjectId()).orElse(null);
        if (sub == null) return List.of();
        
        // Now get all chapters and lessons for this subject
        List<ChapterResponseDTO> result = new ArrayList<>();
        List<ChapterEntity> chapters = chapterRepository.findBySubjectIdOrderByOrderNumberAsc(sub.getId());
        
        for (ChapterEntity chap : chapters) {
            List<LessonEntity> lessons = lessonRepository.findByChapterId(chap.getId());
            
            result.add(ChapterResponseDTO.builder()
                    .id(chap.getId())
                    .chapterName(chap.getChapterName())
                    .lessons(lessons.stream().map(l -> LessonResponseDTO.builder()
                            .id(l.getId())
                            .lessonName(l.getLessonName())
                            .subjectBadge(sub.getName() + " " + sub.getGrade())
                            .videoUrl(l.getVideoUrl())
                            .pdfUrl(l.getPdfUrl())
                            .build()).collect(Collectors.toList()))
                    .build());
        }
        
        return result;
    }

    public LessonResponseDTO getLessonById(Integer lessonId) {
        LessonEntity l = lessonRepository.findById(lessonId).orElse(null);
        if (l == null) return null;
        
        LessonResponseDTO dto = LessonResponseDTO.builder()
                .id(l.getId())
                .lessonName(l.getLessonName())
                .videoUrl(l.getVideoUrl())
                .pdfUrl(l.getPdfUrl())
                .build();
                
        dto.setContent(l.getContent());
        return dto;
    }

    public java.util.List<com.cuong.backend.entity.SubjectEntity> getAllSubjects() {
        return subjectRepository.findAll();
    }

    /**
     * Lấy danh sách lesson_id đã hoàn thành của user
     * trong tập hợp các lesson thuộc một khóa học.
     */
    public List<Integer> getCompletedLessonIds(long userId, List<Integer> lessonIds) {
        if (lessonIds == null || lessonIds.isEmpty()) return List.of();
        return userProgressRepository.findCompletedLessonIds(userId, lessonIds);
    }

    /**
     * Đánh dấu bài học đã hoàn thành.
     * Nếu bản ghi đã tồn tại thì chỉ cập nhật, không tạo mới.
     */
    @Transactional
    public void markLessonCompleted(long userId, int lessonId) {
        UserProgressEntity progress = userProgressRepository
                .findByUserIdAndLessonId(userId, lessonId)
                .orElseGet(() -> {
                    UserProgressEntity p = new UserProgressEntity();
                    p.setUserId(userId);
                    p.setLessonId(lessonId);
                    return p;
                });

        if (!progress.isCompleted()) {
            progress.setCompleted(true);
            progress.setCompletedAt(new Date());
            userProgressRepository.save(progress);
        }
    }
}
