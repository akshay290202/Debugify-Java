package com.debugify.api.service;

import com.debugify.api.dao.UserDao;
import com.debugify.api.entity.User;
import com.debugify.api.exception.BadRequestException;
import com.debugify.api.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@Service
public class UserService {

    @Autowired
    UserDao userDao;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public User findById(Long id) {
        return userDao.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    }

    public User updateUser(Long userId, User currentUser, Map<String, Object> requestData) {
        if(!currentUser.getId().equals(userId)){
            throw new BadRequestException("Update not allowed");
        }

        User user = findById(userId);

        // Only update fields that are actually present in the request
        if (requestData.containsKey("password")) {
            String password = (String) requestData.get("password");
            if (password != null && !password.trim().isEmpty()) {
                if (password.length() < 6) {
                    throw new BadRequestException("Password must be at least 6 characters");
                }
                user.setPassword(passwordEncoder.encode(password));
            }
        }

        if (requestData.containsKey("username")) {
            String username = (String) requestData.get("username");
            if (username != null && !username.trim().isEmpty()) {
                String userName = username.trim();

                if(userName.length() < 7 || userName.length() > 20){
                    throw new BadRequestException("Username must be between 7 and 20 characters");
                }

                if(userName.contains(" ")){
                    throw new BadRequestException("Username cannot contain white spaces");
                }

                if(!userName.equals(userName.toLowerCase())){
                    throw new BadRequestException("username must be lowercase");
                }

                if (!Pattern.matches("^[a-zA-Z0-9]+$", userName)) {
                    throw new BadRequestException("Username can only contain letters and numbers");
                }

                if(userDao.existsByUsername(userName) && !user.getUsername().equals(userName)){
                    throw new BadRequestException("username already exists");
                }

                user.setUsername(userName);
            }
        }

        if (requestData.containsKey("email")) {
            String email = (String) requestData.get("email");
            if (email != null && !email.trim().isEmpty()) {
                if(userDao.existsByEmail(email) && !user.getEmail().equals(email)){
                    throw new BadRequestException("Email Already Exists");
                }
                user.setEmail(email);
            }
        }

        // Only update profile picture if explicitly provided
        if (requestData.containsKey("profilePicture")) {
            String profilePicture = (String) requestData.get("profilePicture");
            if (profilePicture != null) {
                user.setProfilePicture(profilePicture);
            }
        }

        return userDao.save(user);
    }

    public Map<String,Object> getAllUsers(Integer startIndex, Integer limit, String sort) {
        if (startIndex == null) startIndex = 0;
        if (limit == null) limit = 9;
        if (sort == null) sort = "desc";

        Sort sortOrder = sort.equals("asc") ? Sort.by("createdAt").ascending() : Sort.by("createdAt").descending();
        Pageable pageable = PageRequest.of(startIndex / limit, limit, sortOrder);

        // Get users
        Page<User> userPage = userDao.findAll(pageable);
        List<User> users = userPage.getContent();

        long totalUsers = userDao.count();
        LocalDateTime oneMonthAgo = LocalDateTime.now().minusMonths(1);
        long lastMonthUsers = userDao.countUsersCreatedAfter(oneMonthAgo);

        Map<String, Object> response = new HashMap<>();
        response.put("users",users);
        response.put("lastMonthUsers",lastMonthUsers);
        response.put("totalUsers",totalUsers);
        return response;
    }

    public void deleteUser(Long userId) {
        userDao.deleteById(userId);
    }

    public User getUserById(Long userId) {
        return findById(userId);
    }
}
