package com.kyas.wolkandhold.security;

import com.kyas.wolkandhold.entity.UserEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

@ToString
public class CustomUserDetails implements UserDetails {

    @Getter
    private final Long id;

    @Getter
    private final String username;

    @Getter
    private final String password;

    @Getter
    private final Collection<? extends GrantedAuthority> authorities;

    public CustomUserDetails(Long id, String username, String password,
                             Collection<? extends GrantedAuthority> authorities) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.authorities = authorities;
    }

    public static CustomUserDetails fromUserEntity(UserEntity user) {
        return new CustomUserDetails(
                user.getId(),
                user.getUsername(),
                user.getPassword(),
                Collections.emptyList()
        );
    }


}