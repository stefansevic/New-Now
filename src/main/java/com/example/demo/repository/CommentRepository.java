package com.example.demo.repository;

import com.example.demo.model.Comment;
import com.example.demo.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, LocalDateTime> {
    List<Comment> findByReview(Review review);
    List<Comment> findByParent(Comment parent);
}

