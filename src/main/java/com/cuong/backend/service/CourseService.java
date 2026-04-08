package com.cuong.backend.service;

import com.cuong.backend.entity.ChapterEntity;
import com.cuong.backend.entity.LessonEntity;
import com.cuong.backend.entity.SubjectEntity;
import com.cuong.backend.model.response.ChapterResponseDTO;
import com.cuong.backend.model.response.LessonResponseDTO;
import com.cuong.backend.repository.ChapterRepository;
import com.cuong.backend.repository.LessonRepository;
import com.cuong.backend.repository.SubjectRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CourseService {

    @Autowired
    private SubjectRepository subjectRepository;

    @Autowired
    private ChapterRepository chapterRepository;

    @Autowired
    private LessonRepository lessonRepository;

    public List<ChapterResponseDTO> getCourseData(String grade, String subjectName, String keyword) {
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
            // Find all subjects with this name across all grades
            subjects = subjectRepository.findAll().stream()
                    .filter(s -> s.getName().equals(subjectName))
                    .collect(Collectors.toList());
        } else {
            subjects = subjectRepository.findAll();
        }

        List<ChapterResponseDTO> result = new ArrayList<>();

        for (SubjectEntity sub : subjects) {
            List<ChapterEntity> chapters = chapterRepository.findBySubjectIdOrderByOrderNumberAsc(sub.getId());
            
            for (ChapterEntity chap : chapters) {
                // Find lessons in this chapter matching keyword
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
            }
        }

        return result;
    }

    public java.util.List<com.cuong.backend.entity.SubjectEntity> getAllSubjects() {
        return subjectRepository.findAll();
    }
}
