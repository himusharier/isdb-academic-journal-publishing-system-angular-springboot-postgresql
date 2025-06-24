package com.himusharier.ajps_backend.repository;

import com.himusharier.ajps_backend.model.Discussion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DiscussionRepository extends JpaRepository<Discussion, Long> {
    List<Discussion> findBySubmissionId(Long submissionId);
}