package com.himusharier.ajps_backend.repository;

import com.himusharier.ajps_backend.model.Editor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EditorRepository extends JpaRepository<Editor, Long> {
    Editor findByProfileId(Long profileId);
}
