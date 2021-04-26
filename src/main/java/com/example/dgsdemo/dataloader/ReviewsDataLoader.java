package com.example.dgsdemo.dataloader;

import com.example.dgsdemo.generated.DgsConstants;
import com.example.dgsdemo.generated.types.Review;
import com.example.dgsdemo.generated.types.SubmittedReview;
import com.example.dgsdemo.services.ReviewService;
import com.netflix.graphql.dgs.DgsData;
import com.netflix.graphql.dgs.DgsDataLoader;
import com.netflix.graphql.dgs.InputArgument;
import org.dataloader.MappedBatchLoader;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

@DgsDataLoader(name = "reviews")
public class ReviewsDataLoader implements MappedBatchLoader<Integer, List<Review>> {
  private final ReviewService reviewsService;

  public ReviewsDataLoader(ReviewService reviewService) {
    this.reviewsService = reviewService;
  }

  @Override
  public CompletionStage<Map<Integer, List<Review>>> load(Set<Integer> keys) {
    return CompletableFuture.supplyAsync(
        () -> reviewsService.reviewsForShows(new ArrayList<>(keys)));
  }

  @DgsData(parentType = DgsConstants.MUTATION.TYPE_NAME, field = DgsConstants.MUTATION.AddReview)
  public List<Review> addReview(@InputArgument("review") SubmittedReview reviewInput) {
    reviewsService.saveReview(reviewInput);

    List<Review> reviews = reviewsService.reviewsForShows(reviewInput.getShowid());

    return Objects.requireNonNull(reviews);
  }
}
