package com.himusharier.ajps_backend.service;

import com.himusharier.ajps_backend.model.AuthUserDetails;
import com.himusharier.ajps_backend.model.Auth;
import com.himusharier.ajps_backend.repository.AuthRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class JwtAuthDetailsService implements UserDetailsService {

    private final AuthRepository authRepository;

    @Autowired
    public JwtAuthDetailsService(AuthRepository authRepository) {
        this.authRepository = authRepository;
    }

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Auth auth = authRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + email));

        return new AuthUserDetails(auth);
    }

    @Transactional
    public UserDetails loadUserById(Long id) {
        Auth auth = authRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with id: " + id));

        return new AuthUserDetails(auth);
    }
}