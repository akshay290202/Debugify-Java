package com.debugify.api.controller;

import com.debugify.api.entity.User;
import com.debugify.api.service.AuthService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @Autowired
    private AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<String> signup(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String email = request.get("email");
        String password = request.get("password");

        String response = authService.signup(username, email, password);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/signin")
    public ResponseEntity<Map<String, Object>> signin(@RequestBody Map<String, String> request, HttpServletResponse response) {
        String email = request.get("email");
        String password = request.get("password");

        Map<String, Object> result = authService.signIn(email, password, response);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/google")
    public ResponseEntity<User> googleAuth(@RequestBody Map<String, String> request, HttpServletResponse response) {
        String email = request.get("email");
        String name = request.get("name");
        String googlePhotoUrl = request.get("googlePhotoUrl");

        User user = authService.googleAuth(email, name, googlePhotoUrl, response);
        return ResponseEntity.ok(user);
    }
}
