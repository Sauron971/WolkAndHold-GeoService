package com.kyas.wolkandhold.dao;

import com.kyas.wolkandhold.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<UserEntity, Long> {
    UserEntity findByUsername(String username);

    boolean existsByEmail(String email);

    boolean existsByUsername(String username);
}
