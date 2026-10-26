package com.example.Authentication_sys.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    @Autowired
    private JwtFilter jwtFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // disableing Cross-Site Request Forgery protection.
                .authorizeHttpRequests(a -> a
                        // Static frontend files — always public
                        .requestMatchers(
                                "/", "/index.html", "/register.html",
                                "/forgot-password.html", "/dashboard.html", "/admin.html",
                                "/styles.css", "/app.js"
                        ).permitAll()
                        // Public auth endpoints
                        .requestMatchers(
                                "/auth/login",
                                "/auth/register",
                                "/auth/verify-email",
                                "/auth/resend-verification",
                                "/auth/forgot-password",
                                "/auth/reset-password"
                        ).permitAll()
                        // Actuator
                        .requestMatchers("/actuator/health", "/actuator/prometheus").permitAll()
                        // Admin API — requires ADMIN role
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        // Everything else requires authentication
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
