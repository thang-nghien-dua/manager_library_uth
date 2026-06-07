package com.hacker.boooks.entity;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.List;

@Builder @NoArgsConstructor @AllArgsConstructor
@Getter @Setter
@Entity @Table(name = "auth")
public class AuthEntity implements UserDetails {
    @Id
    @Column(nullable = false, length = 50)
    private String username;
    @Column(nullable = false)
    private String password;
    @Column(name = "creation_time")
    private Timestamp creationTime;
    @Column(name = "last_login")
    private Timestamp lastLogin;
    @Column(name = "refresh_token")
    private String refreshToken;
    @Column(name = "reset_token")
    private String resetToken;
    @Column(name = "is_activated")
    private Boolean isActivated;
    
    @Column(length = 20)
    private String role; // e.g. "ROLE_ADMIN", "ROLE_USER"

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(role != null ? role : "ROLE_ADMIN"));
    }
    @Override
    public boolean isAccountNonExpired() { return true; }
    @Override
    public boolean isAccountNonLocked() { return true; }
    @Override
    public boolean isCredentialsNonExpired() { return true; }
    @Override
    public boolean isEnabled() { return true; }
}