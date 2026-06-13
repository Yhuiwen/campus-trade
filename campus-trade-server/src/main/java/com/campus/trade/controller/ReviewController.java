package com.campus.trade.controller;

import com.campus.trade.common.ApiResponse;
import com.campus.trade.common.CurrentUser;
import com.campus.trade.dto.ReviewRequest;
import com.campus.trade.entity.Review;
import com.campus.trade.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {
    private final ReviewService reviewService;

    @PostMapping
    public ApiResponse<Void> create(@Valid @RequestBody ReviewRequest request) {
        reviewService.create(request, CurrentUser.id());
        return ApiResponse.success();
    }

    @GetMapping("/user/{userId}")
    public ApiResponse<List<Review>> byUser(@PathVariable Long userId) {
        return ApiResponse.success(reviewService.byUser(userId));
    }
}
