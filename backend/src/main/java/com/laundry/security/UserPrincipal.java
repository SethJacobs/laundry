package com.laundry.security;

import com.laundry.model.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

public class UserPrincipal implements UserDetails {
    private Long id;
    private String username;
    private String email;
    private String password;
    private boolean isAdmin;
    private boolean isBlocked;
    
    public UserPrincipal(Long id, String username, String email, String password, 
                        boolean isAdmin, boolean isBlocked) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.password = password;
        this.isAdmin = isAdmin;
        this.isBlocked = isBlocked;
    }
    
    public static UserPrincipal create(User user) {
        return new UserPrincipal(
            user.getId(),
            user.getUsername(),
            user.getEmail(),
            user.getPassword(),
            user.isAdmin(),
            user.isBlocked()
        );
    }
    
    public Long getId() {
        return id;
    }
    
    @Override
    public String getUsername() {
        return username;
    }
    
    public String getEmail() {
        return email;
    }
    
    @Override
    public String getPassword() {
        return password;
    }
    
    public boolean isAdmin() {
        return isAdmin;
    }
    
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (isAdmin) {
            return Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN"));
        }
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));
    }
    
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }
    
    @Override
    public boolean isAccountNonLocked() {
        return !isBlocked;
    }
    
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }
    
    @Override
    public boolean isEnabled() {
        return !isBlocked;
    }
}

