package com.example.dgsdemo.datafetchers;


import com.example.dgsdemo.dataloader.ReviewsDataLoader;
import com.example.dgsdemo.generated.DgsConstants;
import com.example.dgsdemo.generated.types.Review;
import com.example.dgsdemo.generated.types.Show;
import com.example.dgsdemo.generated.types.SubmittedReview;
import com.example.dgsdemo.services.ReviewService;
import com.netflix.graphql.dgs.*;
import org.dataloader.DataLoader;
import org.reactivestreams.Publisher;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@DgsComponent
public class ReviewsDataFetcher {
    private final ReviewService reviewService;


    public ReviewsDataFetcher(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @DgsData(parentType = DgsConstants.SHOW.TYPE_NAME, field = DgsConstants.SHOW.Reviews)
    public CompletableFuture<List<Review>> reviews(DgsDataFetchingEnvironment dfe) {
        //Instead of loading a DataLoader by name, we can use the DgsDataFetchingEnvironment and pass in the DataLoader classname.
        DataLoader<Integer, List<Review>> reviewsDataLoader = dfe.getDataLoader(ReviewsDataLoader.class);

        //Because the reviews field is on Show, the getSource() method will return the Show instance.
        Show show = dfe.getSource();

        //Load the reviews from the DataLoader. This call is async and will be batched by the DataLoader mechanism.
        return reviewsDataLoader.load(show.getId());
    }

    //@DgsData(parentType = DgsConstants.MUTATION.TYPE_NAME, field = DgsConstants.MUTATION.AddReview)
    @DgsMutation
    public List<Review> addReview(@InputArgument("review") SubmittedReview review) {
        reviewService.saveReview(review);
        List<Review> reviews = reviewService.reviewsForShows(review.getShowid());
        return Objects.requireNonNull(reviews);
    }

   // @DgsData(parentType = DgsConstants.MUTATION.TYPE_NAME, field = DgsConstants.MUTATION.AddReviews)
    @DgsMutation
    public List<Review> addReviews(@InputArgument(value="reviews",collectionType =SubmittedReview.class) List<SubmittedReview> reviewsInput){
        reviewService.saveReviews(reviewsInput);

        List<Integer> showIds = reviewsInput.stream().map(submittedReview -> submittedReview.getShowid()).collect(Collectors.toList());
        Map<Integer, List<Review>> showReviews = reviewService.reviewsForShows(showIds);
        List<Review> reviews = new ArrayList(showReviews.values());
        return Objects.requireNonNull(reviews);

    }

    //@DgsData(parentType = DgsConstants.SUBSCRIPTION_TYPE, field = DgsConstants.SUBSCRIPTION.ReviewAdded)
    @DgsSubscription
    public Publisher<Review> reviewAdded(@InputArgument("showId") Integer showId) {
        return reviewService.getReviewPublisher();
    }

}
