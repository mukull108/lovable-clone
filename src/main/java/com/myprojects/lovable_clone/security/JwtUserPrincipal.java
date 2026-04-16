package com.myprojects.lovable_clone.security;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class JwtUserPrincipal {
    Long userId;
    String username;
    List<GrantedAuthority> authorities;

    public JwtUserPrincipal(Long userId, String username) {
        this.userId=userId;
        this.username=username;
    }
}
