package com.debugify.api.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.cors(cors -> {}) // Use default CORS configuration from WebConfig
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authz -> authz
                        // Public endpoints
                         .requestMatchers("/api/auth/**").permitAll()
                         .requestMatchers(HttpMethod.GET, "/api/user/test").permitAll()
                         .requestMatchers(HttpMethod.GET, "/api/user/getuser/**").permitAll()
                         .requestMatchers(HttpMethod.GET, "/api/post/getposts").permitAll()
                         .requestMatchers(HttpMethod.GET, "/api/post/search").permitAll()
                         .requestMatchers(HttpMethod.GET, "/api/comment/getpostcomments/**").permitAll()
                         .requestMatchers("/", "/static/**", "/*.js", "/*.css", "/*.html", "/*.ico", "/*.png", "/*.jpg").permitAll()
                         .requestMatchers(HttpMethod.GET, "/api/user/getuser/**").permitAll() 
                         // Protected endpoints
                         .requestMatchers(HttpMethod.GET, "/api/user/getusers").hasRole("ADMIN")
                         .requestMatchers(HttpMethod.GET, "/api/comment/getcomments").hasRole("ADMIN")
                         .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }


}