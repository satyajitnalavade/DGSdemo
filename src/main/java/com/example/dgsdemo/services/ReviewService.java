package com.example.dgsdemo.services;


import com.example.dgsdemo.generated.types.Review;
import com.example.dgsdemo.generated.types.SubmittedReview;
import org.reactivestreams.Publisher;

import java.util.List;
import java.util.Map;

/**
 * @author satya
 */
public interface ReviewService {

    List<Review> reviewsForShows(Integer showId);
    Map<Integer,List<Review>> reviewsForShows(List<Integer>showIds);
    void saveReview(SubmittedReview reviewInput);
    Publisher<Review> getReviewPublisher();
}
