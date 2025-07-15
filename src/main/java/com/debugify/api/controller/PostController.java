package com.debugify.api.controller;

import com.debugify.api.entity.Post;
import com.debugify.api.entity.User;
import com.debugify.api.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/post")
public class PostController {

    @Autowired
    PostService postService;

    @PostMapping("/create")
    public ResponseEntity<Post> createPost(@RequestBody Map<String, String> request) {
        User currentUser = getCurrentUser();
        String title = request.get("title");
        String content = request.get("content");
        String category = request.get("category");

        Post post = postService.createPost(title, content, category, currentUser);
        return ResponseEntity.status(201).body(post);
    }

    @GetMapping("/getposts")
    public ResponseEntity<Map<String, Object>> getPosts(
            @RequestParam(required = false) Integer startIndex,
            @RequestParam(required = false) Integer limit,
            @RequestParam(required = false) String order,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String slug,
            @RequestParam(required = false) Long postId,
            @RequestParam(required = false) String searchTerm) {

        Map<String, Object> response = postService.getPosts(startIndex, limit, order, userId, category, slug, postId, searchTerm);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/deletepost/{postId}/{userId}")
    public ResponseEntity<String> deletePost(@PathVariable Long postId, @PathVariable Long userId) {
        User currentUser = getCurrentUser();
        postService.deletePost(postId, userId, currentUser);
        return ResponseEntity.ok("The Post has been deleted");
    }

    @PutMapping("/updatepost/{postId}/{userId}")
    public ResponseEntity<Post> updatePost(@PathVariable Long postId, @PathVariable Long userId, @RequestBody Map<String, String> request) {
        User currentUser = getCurrentUser();
        String title = request.get("title");
        String content = request.get("content");
        String category = request.get("category");

        Post updatedPost = postService.updatePost(postId, userId, currentUser, title, content, category);
        return ResponseEntity.ok(updatedPost);
    }

    @GetMapping("/getpost/{postId}")
    public ResponseEntity<Post> getPost(@PathVariable Long postId) {
        Post post = postService.getPost(postId);
        return ResponseEntity.ok(post);
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return (User) authentication.getPrincipal();
    }

    @GetMapping("/search")
    public ResponseEntity<Map<String, Object>> searchPosts(
            @RequestParam(required = false) String searchTerm,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "12") Integer size) {
        
        Map<String, Object> response = postService.searchPosts(searchTerm, sort, category, page, size);
        return ResponseEntity.ok(response);
    }
}
