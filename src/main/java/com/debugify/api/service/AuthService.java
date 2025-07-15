package com.debugify.api.service;

import com.debugify.api.config.JwtUtils;
import com.debugify.api.dao.UserDao;
import com.debugify.api.entity.User;
import com.debugify.api.exception.BadRequestException;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseCookie;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Random;

@Service
@Transactional
public class AuthService {

    @Autowired
    private UserDao userDao;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtils jwtUtils;

    public String signup(String username, String email, String password) {
        // check if any field is Missing
        if (username == null || username.trim().isEmpty() ||
                email == null || email.trim().isEmpty() ||
                password == null || password.trim().isEmpty()) {
            throw new BadRequestException("All fields are required!");
        }

        // Check if user already exists
        if (userDao.existsByEmail(email)) {
            throw new BadRequestException("Email already exists");
        }

        if (userDao.existsByUsername(username)) {
            throw new BadRequestException("Username already exists");
        }

        User user = new User();
        user.setUsername(username.trim());
        user.setEmail(email.trim());
        user.setPassword(passwordEncoder.encode(password));

        userDao.save(user);
        return "Signup Successful !!";
    }

    public User signIn(String email, String password, HttpServletResponse response) {
        // Validate required fields
        if (email == null || email.trim().isEmpty() ||
                password == null || password.trim().isEmpty()) {
            throw new BadRequestException("All fields are required!");
        }

        // Find user by email
        User user = userDao.findByEmail(email.trim())
                .orElseThrow(() -> new BadRequestException("User not found"));

        // Validate password
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new BadRequestException("Invalid Password");
        }

        String token = jwtUtils.generateToken(user.getId(), user.getIsAdmin());

        ResponseCookie cookie = ResponseCookie.from("access_token", token)
                .httpOnly(true)
                .secure(false) // Set to true in production with HTTPS
                .path("/")
                .maxAge(24 * 60 * 60)
                .sameSite("Lax")
                .build();

        response.addHeader("Set-Cookie", cookie.toString());

        return user;
    }

    public User googleAuth(String email, String name, String googlePhotoUrl, HttpServletResponse response) {
        // Try to find existing user
        User user = userDao.findByEmail(email).orElse(null);

        if (user != null) {
            // User exists, generate token and return
            String token = jwtUtils.generateToken(user.getId(), user.getIsAdmin());

            ResponseCookie cookie = ResponseCookie.from("access_token", token)
                    .httpOnly(true)
                    .secure(false) // change to true in production
                    .path("/")
                    .maxAge(24 * 60 * 60)
                    .sameSite("Lax")
                    .build();

            response.addHeader("Set-Cookie", cookie.toString());
            return user;
        } else {
            // Create new user
            String generatedPassword = generateRandomPassword();
            String generatedUsername = generateUniqueUsername(name);

            User newUser = new User();
            newUser.setUsername(generatedUsername);
            newUser.setEmail(email);
            newUser.setPassword(passwordEncoder.encode(generatedPassword));
            newUser.setProfilePicture(googlePhotoUrl);

            userDao.save(newUser);

            // Generate token for new user
            String token = jwtUtils.generateToken(newUser.getId(), newUser.getIsAdmin());

            ResponseCookie cookie = ResponseCookie.from("access_token", token)
                    .httpOnly(true)
                    .secure(false) // set to true in prod with https
                    .path("/")
                    .maxAge(24 * 60 * 60)
                    .sameSite("Lax")
                    .build();

            response.addHeader("Set-Cookie", cookie.toString());
            return newUser;
        }
    }

    private String generateRandomPassword() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        Random random = new Random();
        StringBuilder password = new StringBuilder();

        for (int i = 0; i < 16; i++) {
            password.append(chars.charAt(random.nextInt(chars.length())));
        }

        return password.toString();
    }

    private String generateUniqueUsername(String name) {
        String baseUsername = name.toLowerCase().replaceAll("\\s+", "");
        String username = baseUsername;

        // Add random numbers until we find a unique username
        Random random = new Random();
        while (userDao.existsByUsername(username)) {
            username = baseUsername + String.format("%04d", random.nextInt(10000));
        }

        return username;
    }
}
