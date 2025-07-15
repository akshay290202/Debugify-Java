package com.debugify.api.service;

import com.debugify.api.dao.PostDao;
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

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PostService {

    @Autowired
    private PostDao postDao;

    @Autowired
    private UserService userService;

    public Post createPost(String title, String content, String category, User currentUser) {
        // Validate required fields
        if (title == null || title.trim().isEmpty() ||
                content == null || content.trim().isEmpty()) {
            throw new BadRequestException("Please provide all the fields");
        }

        // Generate slug from title
        String slug = generateSlug(title);

        // Check if title or slug already exists
        if (postDao.existsByTitle(title.trim())) {
            throw new BadRequestException("Post with this title already exists");
        }

        if (postDao.existsBySlug(slug)) {
            throw new BadRequestException("Post with this slug already exists");
        }

        // Create new post
        Post post = new Post();
        post.setTitle(title.trim());
        post.setContent(content.trim());
        post.setCategory(category != null ? category.trim() : "uncategorized");
        post.setSlug(slug);
        post.setUser(currentUser);
        post.setUserId(currentUser.getId());

        return postDao.save(post);
    }

    private String generateSlug(String title) {
        return title.toLowerCase()
                .replaceAll("[^a-zA-Z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-")
                .trim();
    }

    public Post getPost(Long postId) {
        return postDao.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found"));
    }

    public Map<String, Object> getPosts(Integer startIndex, Integer limit, String order, Long userId, String category, String slug, Long postId, String searchTerm) {
        // Default values
        if (startIndex == null) startIndex = 0;
        if (limit == null) limit = 12;
        if (order == null) order = "desc";

        Page<Post> postPage;

        if(userId != null){
            // For JPA method, use Java property name
            Sort sortOrder = order.equals("asc") ? Sort.by("updatedAt").ascending() : Sort.by("updatedAt").descending();
            Pageable pageable = PageRequest.of(startIndex / limit, limit, sortOrder);
            postPage = postDao.findByUserId(userId, pageable);
            List<Post> posts = postPage.getContent();
            Map<String, Object> response = new HashMap<>();
            response.put("posts", posts);
            response.put("totalPosts", postPage.getTotalElements());
            return response;
        }

        // For native SQL queries, use database column name
        Sort sortOrder = order.equals("asc") ? Sort.by("updated_at").ascending() : Sort.by("updated_at").descending();
        Pageable pageable = PageRequest.of(startIndex / limit, limit, sortOrder);

        // Handle specific post by ID
        if (postId != null) {
            Post post = postDao.findById(postId)
                    .orElseThrow(() -> new ResourceNotFoundException("Post not found"));
            Map<String, Object> response = new HashMap<>();
            response.put("posts", List.of(post));
            response.put("totalPosts", 1L);
            response.put("lastMonthPosts", 0L);
            return response;
        }

        // Handle specific post by slug
        if (slug != null && !slug.trim().isEmpty()) {
            Post post = postDao.findBySlug(slug.trim())
                    .orElseThrow(() -> new ResourceNotFoundException("Post not found"));
            Map<String, Object> response = new HashMap<>();
            response.put("posts", List.of(post));
            response.put("totalPosts", 1L);
            response.put("lastMonthPosts", 0L);
            return response;
        }

        // Use custom query for filtering
        postPage = postDao.findWithFilters(searchTerm, category, pageable);

        List<Post> posts = postPage.getContent();

        // Count statistics
        long totalPosts = postDao.count();
        LocalDateTime oneMonthAgo = LocalDateTime.now().minusMonths(1);
        long lastMonthPosts = postDao.countPostsCreatedAfter(oneMonthAgo);

        // Prepare response
        Map<String, Object> response = new HashMap<>();
        response.put("posts", posts);
        response.put("totalPosts", totalPosts);
        response.put("lastMonthPosts", lastMonthPosts);

        return response;
    }


    public void deletePost(Long postId, Long userId, User currentUser) {
        // Check if current user owns the post
        if (!currentUser.getId().equals(userId)) {
            throw new BadRequestException("You are not allowed to delete this post");
        }

        Post post = postDao.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found"));

        // Double check ownership
        if (!post.getUserId().equals(currentUser.getId())) {
            throw new BadRequestException("You are not allowed to delete this post");
        }

        postDao.delete(post);
    }

    public Post updatePost(Long postId, Long userId, User currentUser, String title, String content, String category) {
        // Check if current user owns the post
        if (!currentUser.getId().equals(userId)) {
            throw new BadRequestException("You are not allowed to update this post");
        }

        Post post = postDao.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found"));

        // Double check ownership
        if (!post.getUserId().equals(currentUser.getId())) {
            throw new BadRequestException("You are not allowed to update this post");
        }

        // Update fields if provided
        if (title != null && !title.trim().isEmpty()) {
            post.setTitle(title.trim());
        }

        if (content != null && !content.trim().isEmpty()) {
            post.setContent(content.trim());
        }

        if (category != null && !category.trim().isEmpty()) {
            post.setCategory(category.trim());
        }

        return postDao.save(post);
    }

    public Post findById(Long id) {
        return postDao.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found with id: " + id));
    }

    public Map<String, Object> searchPosts(String searchTerm, String sort, String category, Integer page, Integer size) {
        // Handle null sort parameter
        if (sort == null || sort.equals("null")) {
            sort = "desc"; // default to descending order
        }

        // Create pageable with sorting
        Sort sortOrder = sort.equals("asc") ? Sort.by("updated_at").ascending() : Sort.by("updated_at").descending();
        Pageable pageable = PageRequest.of(page, size, sortOrder);

        // Handle null category parameter
        if (category != null && category.equals("null")) {
            category = null;
        }

        // Handle empty searchTerm
        if (searchTerm != null && searchTerm.trim().isEmpty()) {
            searchTerm = null;
        }

        // Use existing filter method
        Page<Post> postPage = postDao.findWithFilters(searchTerm, category, pageable);
        List<Post> posts = postPage.getContent();

        // Count statistics
        long totalPosts = postPage.getTotalElements();
        LocalDateTime oneMonthAgo = LocalDateTime.now().minusMonths(1);
        long lastMonthPosts = postDao.countPostsCreatedAfter(oneMonthAgo);

        // Prepare response
        Map<String, Object> response = new HashMap<>();
        response.put("posts", posts);
        response.put("totalPosts", totalPosts);
        response.put("currentPage", page);
        response.put("totalPages", postPage.getTotalPages());
        response.put("pageSize", size);
        response.put("hasNext", postPage.hasNext());
        response.put("hasPrevious", postPage.hasPrevious());
        response.put("lastMonthPosts", lastMonthPosts);

        return response;
    }
}
