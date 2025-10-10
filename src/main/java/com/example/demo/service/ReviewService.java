package com.example.demo.service;

import com.example.demo.model.Comment;
import com.example.demo.model.Review;
import com.example.demo.repository.CommentRepository;
import com.example.demo.repository.ReviewRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final CommentRepository commentRepository;

    public ReviewService(ReviewRepository reviewRepository, CommentRepository commentRepository) {
        this.reviewRepository = reviewRepository;
        this.commentRepository = commentRepository;
    }

    public static class ReviewWithComments {
        private Review review;
        private List<Comment> comments;

        public ReviewWithComments(Review review, List<Comment> comments) {
            this.review = review;
            this.comments = comments;
        }

        public Review getReview() {
            return review;
        }

        public void setReview(Review review) {
            this.review = review;
        }

        public List<Comment> getComments() {
            return comments;
        }

        public void setComments(List<Comment> comments) {
            this.comments = comments;
        }
    }

    public List<ReviewWithComments> getReviewsWithComments(String locationName) {
        // This method will be used by controller to get reviews with their comments
        return reviewRepository.findAll().stream()
            .map(review -> {
                List<Comment> comments = commentRepository.findByReview(review);
                return new ReviewWithComments(review, comments);
            })
            .toList();
    }
}

