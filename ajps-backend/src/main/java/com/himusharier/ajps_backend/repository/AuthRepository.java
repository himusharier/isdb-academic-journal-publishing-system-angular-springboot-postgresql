package com.himusharier.ajps_backend.repository;

import com.himusharier.ajps_backend.constants.UserRole;
import com.himusharier.ajps_backend.model.Auth;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AuthRepository extends JpaRepository<Auth, Long> {

    Optional<Auth> findByEmail(String email);

    boolean existsByEmail(String email);

    List<Auth> findByUserRole(UserRole userRole);

    Optional<Auth> findByUserId(Long userId);
}
