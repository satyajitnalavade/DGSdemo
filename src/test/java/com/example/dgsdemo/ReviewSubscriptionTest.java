package com.example.dgsdemo;

import com.example.dgsdemo.datafetchers.ReviewsDataFetcher;
import com.example.dgsdemo.generated.client.AddReviewGraphQLQuery;
import com.example.dgsdemo.generated.client.AddReviewProjectionRoot;
import com.example.dgsdemo.generated.types.Review;
import com.example.dgsdemo.generated.types.SubmittedReview;
import com.example.dgsdemo.scalars.DateTimeScalar;
import com.example.dgsdemo.services.DefaultReviewService;
import com.example.dgsdemo.services.ShowService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.netflix.graphql.dgs.DgsQueryExecutor;
import com.netflix.graphql.dgs.autoconfig.DgsAutoConfiguration;
import com.netflix.graphql.dgs.client.codegen.GraphQLQueryRequest;
import graphql.ExecutionResult;
import org.junit.jupiter.api.Test;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest(classes = {DefaultReviewService.class, ReviewsDataFetcher.class, DgsAutoConfiguration.class, DateTimeScalar.class})
public class ReviewSubscriptionTest {
    @Autowired
    DgsQueryExecutor dgsQueryExecutor;

    @MockBean
    ShowService showsService;

    @Test
    void reviewSubscription() {
        ExecutionResult executionResult = dgsQueryExecutor.execute("subscription { reviewAdded(showId: 1) {starScore} }");
        Publisher<ExecutionResult> reviewPublisher = executionResult.getData();
        List<Review> reviews = new CopyOnWriteArrayList<>();

        reviewPublisher.subscribe(new Subscriber<ExecutionResult>() {
            @Override
            public void onSubscribe(Subscription s) {
                s.request(2);
            }

            @Override
            public void onNext(ExecutionResult executionResult) {
                if (executionResult.getErrors().size() > 0) {
                    System.out.println(executionResult.getErrors());
                }
                Map<String, Object> review = executionResult.getData();
                reviews.add(new ObjectMapper().convertValue(review.get("reviewAdded"), Review.class));
            }

            @Override
            public void onError(Throwable t) {
            }

            @Override
            public void onComplete() {
            }
        });

        assertThat(reviews.size()).isEqualTo(0);

        addReview();
        addReview();

        assertThat(reviews.size()).isEqualTo(2);
    }



    private void addReview() {
        GraphQLQueryRequest graphQLQueryRequest = new GraphQLQueryRequest(
                AddReviewGraphQLQuery.newRequest()
                        .review(
                                SubmittedReview.newBuilder()
                                        .showid(1)
                                        .username("testuser")
                                        .startScore(5).build())
                        .build(),
                new AddReviewProjectionRoot()
                        .username()
                        .starScore());
        dgsQueryExecutor.execute(graphQLQueryRequest.serialize());
    }


}
