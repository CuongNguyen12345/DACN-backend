package com.cuong.backend.repository;

import com.cuong.backend.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {
    List<UserEntity> findAllByUserName(String userName);
    UserEntity findOneByUserName(String userName);
}
