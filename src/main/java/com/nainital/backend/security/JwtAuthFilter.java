package com.nainital.backend.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserDetailsServiceImpl userDetailsService;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        return path.startsWith("/api/auth/")
                || path.startsWith("/api/seller/auth/")
                || path.startsWith("/api/delivery/auth/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            try {
                if (jwtUtil.validateToken(token)) {
                    UserDetails userDetails;
                    if (jwtUtil.isAdminToken(token)) {
                        String adminId = jwtUtil.extractUserId(token);
                        String role = jwtUtil.extractRole(token);
                        if (role == null || role.isBlank()) {
                            role = "ADMIN";
                        }
                        var authorities = List.of(new SimpleGrantedAuthority("ROLE_" + role));
                        userDetails = new org.springframework.security.core.userdetails.User(
                                adminId, "", authorities);
                    } else if (jwtUtil.isSellerToken(token)) {
                        String sellerId = jwtUtil.extractUserId(token);
                        var authorities = List.of(new SimpleGrantedAuthority("ROLE_SELLER"));
                        userDetails = new org.springframework.security.core.userdetails.User(
                                sellerId, "", authorities);
                    } else if (jwtUtil.isDeliveryToken(token)) {
                        String partnerId = jwtUtil.extractUserId(token);
                        var authorities = List.of(new SimpleGrantedAuthority("ROLE_DELIVERY"));
                        userDetails = new org.springframework.security.core.userdetails.User(
                                partnerId, "", authorities);
                    } else {
                        String userId = jwtUtil.extractUserId(token);
                        userDetails = userDetailsService.loadUserByUsername(userId);
                    }

                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails, null, userDetails.getAuthorities());
                    authentication.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request));

                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            } catch (Exception ignored) {
                // Invalid token — let the request proceed unauthenticated
            }
        }

        filterChain.doFilter(request, response);
    }
}
