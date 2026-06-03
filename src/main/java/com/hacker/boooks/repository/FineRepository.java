package com.hacker.boooks.repository;

import com.hacker.boooks.entity.FineEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FineRepository extends JpaRepository<FineEntity, Integer> {
}