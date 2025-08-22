package com.kyas.wolkandhold.security;


import com.kyas.wolkandhold.services.PostUserDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
@Component
public class AuthTokenFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;
    private final PostUserDetailsService userDetailsService;

    public AuthTokenFilter(JwtUtils jwtUtils, PostUserDetailsService userDetailsService) {
        this.jwtUtils = jwtUtils;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain filterChain
    ) throws ServletException, IOException {

        String header = request.getHeader("Authorization");
        // Ищем токен в заголовке
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7); // убираем "Bearer "
            if (jwtUtils.validate(token)) {
                String username = jwtUtils.getUsername(token);
                CustomUserDetails ud = userDetailsService.loadUserByUsername(username);

                // Создаём аутентификацию и кладём в SecurityContext
                UsernamePasswordAuthenticationToken auth =
                        new UsernamePasswordAuthenticationToken(ud, null, ud.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        }
        filterChain.doFilter(request, response);
    }
}
