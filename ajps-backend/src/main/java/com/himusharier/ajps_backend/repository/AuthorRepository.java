package com.himusharier.ajps_backend.repository;

import com.himusharier.ajps_backend.model.Author;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuthorRepository extends JpaRepository<Author, Long> {

    void deleteBySubmissionIdAndId(Long submissionId, Long authorId);

//    List<Author> findBySubmissionId(String submissionId);

}
