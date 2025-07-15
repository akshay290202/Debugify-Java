package com.debugify.api.controller;

import com.debugify.api.entity.Comment;
import com.debugify.api.entity.User;
import com.debugify.api.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/comment")
public class CommentController {
    @Autowired
    private CommentService commentService;

    @PostMapping("/create")
    public ResponseEntity<Comment> createComment(@RequestBody Map<String, Object> request) {
        User currentUser = getCurrentUser();
        String content = (String) request.get("content");
        Long postId = Long.valueOf(request.get("postId").toString());
        Long userId = Long.valueOf(request.get("userId").toString());

        Comment comment = commentService.createComment(content, postId, userId, currentUser);
        return ResponseEntity.ok(comment);
    }

    @GetMapping("/getpostcomments/{postId}")
    public ResponseEntity<List<Comment>> getPostComments(@PathVariable Long postId) {
        List<Comment> comments = commentService.getPostComments(postId);
        return ResponseEntity.ok(comments);
    }

    @GetMapping("/getcomments")
    public ResponseEntity<Map<String, Object>> getComments(
            @RequestParam(required = false) Integer startIndex,
            @RequestParam(required = false) Integer limit,
            @RequestParam(required = false) String sort) {

        User currentUser = getCurrentUser();
        Map<String, Object> response = commentService.getComments(startIndex, limit, sort, currentUser);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/likecomment/{commentId}")
    public ResponseEntity<Comment> likeComment(@PathVariable Long commentId) {
        User currentUser = getCurrentUser();
        Comment comment = commentService.likeComment(commentId, currentUser);
        return ResponseEntity.ok(comment);
    }

    @PutMapping("/editcomment/{commentId}")
    public ResponseEntity<Comment> editComment(@PathVariable Long commentId, @RequestBody Map<String, String> request) {
        User currentUser = getCurrentUser();
        String content = request.get("content");

        Comment comment = commentService.editComment(commentId, content, currentUser);
        return ResponseEntity.ok(comment);
    }

    @DeleteMapping("/deletecomment/{commentId}")
    public ResponseEntity<String> deleteComment(@PathVariable Long commentId) {
        User currentUser = getCurrentUser();
        commentService.deleteComment(commentId, currentUser);
        return ResponseEntity.ok("Comment deleted Successfully");
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return (User) authentication.getPrincipal();
    }
}
