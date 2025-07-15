package com.debugify.api.controller;


import com.debugify.api.entity.User;
import com.debugify.api.service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    UserService userService;

    @PutMapping("/update/{userId}")
    public ResponseEntity<User> updateUser(@PathVariable Long userId, @RequestBody Map<String, Object> requestData){
        User currentUser = getCurrentUser();
        User updatedUser = userService.updateUser(userId, currentUser, requestData);
        return new ResponseEntity<>(updatedUser, HttpStatus.OK);
    }

    @GetMapping("/getusers")
    public ResponseEntity<Map<String,Object>> getAllUsers(@RequestParam(required = false) Integer startIndex,
                                                  @RequestParam(required = false) Integer limit,
                                                  @RequestParam(required = false) String sort){
        Map<String,Object> response = userService.getAllUsers(startIndex,limit,sort);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/getuser/{userId}")
    public ResponseEntity<User> getUserById(@PathVariable Long userId) {
        User user = userService.getUserById(userId);
        return ResponseEntity.ok(user);
    }

    @PostMapping("/signout/")
    public ResponseEntity<String> signout(HttpServletResponse response) {
        try {
            // Clear the access_token cookie
            Cookie cookie = new Cookie("access_token", null);
            cookie.setMaxAge(0);
            cookie.setHttpOnly(true);
            cookie.setPath("/");
            response.addCookie(cookie);
            
            return ResponseEntity.ok("Sign Out Successful");
        } catch (Exception error) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Sign out failed");
        }
    }

    @DeleteMapping("/delete/{userId}")
    public ResponseEntity<String> deleteUser(@PathVariable Long userId) {
        userService.deleteUser(userId);
        return ResponseEntity.ok("User deleted successfully");
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return (User) authentication.getPrincipal();
    }


}
