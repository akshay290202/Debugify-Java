package com.debugify.api.service;

import com.debugify.api.dao.CommentDao;
import com.debugify.api.dao.CommentLikeDao;
import com.debugify.api.entity.Comment;
import com.debugify.api.entity.CommentLike;
import com.debugify.api.entity.Post;
import com.debugify.api.entity.User;
import com.debugify.api.exception.BadRequestException;
import com.debugify.api.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class CommentService {
    @Autowired
    private CommentDao commentDao;

    @Autowired
    private CommentLikeDao commentLikeDao;

    @Autowired
    private PostService postService;

    @Autowired
    private UserService userService;

    public Comment createComment(String content, Long postId, Long userId, User currentUser) {

        // Validate required fields
        if (content == null || content.trim().isEmpty()) {
            throw new BadRequestException("Content is required");
        }

        if (postId == null) {
            throw new BadRequestException("Post ID is required");
        }

        // Get post to ensure it exists
        Post post = postService.findById(postId);

        // Create comment
        Comment comment = new Comment();
        comment.setContent(content.trim());
        comment.setUser(currentUser);
        comment.setPost(post);
        comment.setUserId(currentUser.getId());
        comment.setPostId(postId);

        return commentDao.save(comment);
    }

    public List<Comment> getPostComments(Long postId) {
        return commentDao.findByPostIdOrderByCreatedAtDesc(postId);
    }

    public Comment likeComment(Long commentId, User currentUser) {
        Comment comment = commentDao.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found"));

        // Check if user already liked this comment
        boolean alreadyLiked = commentLikeDao.existsByCommentIdAndUserId(commentId, currentUser.getId());

        if (alreadyLiked) {
            // Unlike the comment
            CommentLike like = commentLikeDao.findByCommentIdAndUserId(commentId, currentUser.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Like not found"));
            
            // Remove from the collection first
            comment.getLikes().removeIf(l -> l.getId() != null && l.getId().equals(like.getId()));
            
            // Delete from database
            commentLikeDao.delete(like);
            
            // Update like count
            comment.setNumberOfLikes(Math.max(0, comment.getNumberOfLikes() - 1));
            commentDao.save(comment);
        } else {
            // Like the comment
            CommentLike like = new CommentLike();
            like.setComment(comment);
            like.setUser(currentUser);
            CommentLike savedLike = commentLikeDao.save(like);
            
            // Add to the collection
            comment.getLikes().add(savedLike);
            
            // Update like count
            comment.setNumberOfLikes(comment.getNumberOfLikes() + 1);
            commentDao.save(comment);
        }

        // Return the updated comment (no need to fetch again since we properly managed the collection)
        return comment;
    }

    public Comment editComment(Long commentId, String content, User currentUser) {
        Comment comment = commentDao.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found"));

        // Check permission
        if (!comment.getUserId().equals(currentUser.getId()) && !currentUser.getIsAdmin()) {
            throw new BadRequestException("You are not allowed to edit this comment");
        }

        // Validate content
        if (content == null || content.trim().isEmpty()) {
            throw new BadRequestException("Content is required");
        }

        comment.setContent(content.trim());
        return commentDao.save(comment);
    }

    public void deleteComment(Long commentId, User currentUser) {
        Comment comment = commentDao.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found"));

        // Check permission
        if (!comment.getUserId().equals(currentUser.getId()) && !currentUser.getIsAdmin()) {
            throw new BadRequestException("You are not allowed to delete this comment");
        }

        commentDao.delete(comment);
    }

    public Map<String, Object> getComments(Integer startIndex, Integer limit, String sort, User currentUser) {
        // Check if user is admin
        if (!currentUser.getIsAdmin()) {
            throw new BadRequestException("You are not allowed to view all the comments");
        }

        // Default values
        if (startIndex == null) startIndex = 0;
        if (limit == null) limit = 9;
        if (sort == null) sort = "desc";

        // Create pageable
        Sort sortOrder = sort.equals("asc") ? Sort.by("createdAt").ascending() : Sort.by("createdAt").descending();
        Pageable pageable = PageRequest.of(startIndex / limit, limit, sortOrder);

        // Get comments
        Page<Comment> commentPage = commentDao.findAll(pageable);
        List<Comment> comments = commentPage.getContent();

        // Count statistics
        long totalComments = commentDao.count();
        LocalDateTime oneMonthAgo = LocalDateTime.now().minusMonths(1);
        long lastMonthComments = commentDao.countCommentsCreatedAfter(oneMonthAgo);

        // Prepare response
        Map<String, Object> response = new HashMap<>();
        response.put("Comments", comments); // Note: Capital C to match original API
        response.put("total", totalComments);
        response.put("lastMonthComments", lastMonthComments);

        return response;
    }

    public Comment findById(Long id) {
        return commentDao.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found with id: " + id));
    }
}
