package com.kyas.wolkandhold.services;

import com.kyas.wolkandhold.dao.UserRepository;
import com.kyas.wolkandhold.entity.UserEntity;
import com.kyas.wolkandhold.security.CustomUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class PostUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;


    @Override
    public CustomUserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserEntity user = userRepository.findByUsername(username);

        if (user == null ) {
            throw new UsernameNotFoundException("User not found");
        }

        return CustomUserDetails.fromUserEntity(user);
    }
}
