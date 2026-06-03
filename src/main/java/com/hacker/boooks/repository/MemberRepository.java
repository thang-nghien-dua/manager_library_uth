package com.hacker.boooks.repository;

import com.hacker.boooks.entity.MemberEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MemberRepository extends JpaRepository<MemberEntity, Integer> {
    List<MemberEntity> findByNameContainingIgnoreCaseOrEmailContainingIgnoreCaseOrPhoneNumberContainingIgnoreCase(String nameKeyword, String emailKeyword, String phoneNumberKeyword);
}