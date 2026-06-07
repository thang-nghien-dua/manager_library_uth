package com.hacker.boooks.repository;

import com.hacker.boooks.entity.ReviewEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<ReviewEntity, Integer> {
    List<ReviewEntity> findByBookId(Integer bookId);
    List<ReviewEntity> findByMemberId(Integer memberId);
    boolean existsByBookIdAndMemberId(Integer bookId, Integer memberId);
}
