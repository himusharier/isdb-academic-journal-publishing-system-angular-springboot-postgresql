package com.himusharier.ajps_backend.model;

import com.himusharier.ajps_backend.constants.UserRole;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

public record AuthUserDetails(Auth auth) implements UserDetails {

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + auth.getUserRole().name()));
    }

    @Override
    public String getPassword() {
        return auth.getPassword();
    }

    public Long getId() {
        return auth.getUserId();
    }

    public String getEmail() {
        return auth.getEmail();
    }

    public UserRole getRole() {
        return auth.getUserRole();
    }

    @Override
    public String getUsername() {
        return auth.getEmail(); // Required method
    }
}