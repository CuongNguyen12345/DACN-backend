package com.cuong.backend.repository;

import com.cuong.backend.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {
    boolean existsByEmail(String email);

    boolean existsByUserName(String userName);

    List<UserEntity> findAllByUserName(String userName);

    UserEntity findOneByEmail(String email);

    /** Tìm kiếm theo keyword (tên / email) và lọc theo role. */
    @Query("""
            SELECT u FROM UserEntity u
            WHERE (:keyword IS NULL OR :keyword = '' OR
                   LOWER(u.userName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
                   LOWER(u.email)    LIKE LOWER(CONCAT('%', :keyword, '%')))
            AND   (:role IS NULL OR :role = '' OR LOWER(u.role) = LOWER(:role))
            ORDER BY u.createdDate DESC
            """)
    List<UserEntity> searchUsers(@Param("keyword") String keyword,
                                 @Param("role") String role);
}
