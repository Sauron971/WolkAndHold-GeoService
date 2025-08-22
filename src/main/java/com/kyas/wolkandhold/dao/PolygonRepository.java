package com.kyas.wolkandhold.dao;

import com.kyas.wolkandhold.entity.PolygonEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PolygonRepository extends JpaRepository<PolygonEntity, Long> {
    Optional<PolygonEntity> findUserById(long userId);
}
