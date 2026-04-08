package com.cuong.backend.repository;

import com.cuong.backend.entity.SubjectEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SubjectRepository extends JpaRepository<SubjectEntity, Integer> {
    Optional<SubjectEntity> findByNameAndGrade(String name, String grade);
    java.util.List<SubjectEntity> findByGrade(String grade);
}
