package com.example.dgsdemo;


import com.example.dgsdemo.datafetchers.ReviewsDataFetcher;
import com.example.dgsdemo.datafetchers.ShowDataFetcher;
import com.example.dgsdemo.dataloader.ReviewsDataLoader;
import com.example.dgsdemo.generated.client.AddReviewGraphQLQuery;
import com.example.dgsdemo.generated.client.AddReviewProjectionRoot;
import com.example.dgsdemo.generated.client.ShowsGraphQLQuery;
import com.example.dgsdemo.generated.client.ShowsProjectionRoot;
import com.example.dgsdemo.generated.types.Review;
import com.example.dgsdemo.generated.types.Show;
import com.example.dgsdemo.generated.types.SubmittedReview;
import com.example.dgsdemo.scalars.DateTimeScalar;
import com.example.dgsdemo.services.ReviewService;
import com.example.dgsdemo.services.ShowService;
import com.jayway.jsonpath.TypeRef;
import com.netflix.graphql.dgs.DgsQueryExecutor;
import com.netflix.graphql.dgs.autoconfig.DgsAutoConfiguration;
import com.netflix.graphql.dgs.client.codegen.GraphQLQueryRequest;
import graphql.ExecutionResult;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@SpringBootTest(classes = {DgsAutoConfiguration.class, ShowDataFetcher.class, ReviewsDataFetcher.class, ReviewsDataLoader.class, DateTimeScalar.class})
public class ShowDatafetcherTest {

    @Autowired
    DgsQueryExecutor dgsQueryExecutor;

    @MockBean
    ShowService showsService;

    @MockBean
    ReviewService reviewsService;

    @BeforeEach
    public void before() {
        Mockito.when(showsService.shows()).thenAnswer(invocation -> Collections.singletonList(Show.newBuilder().id(1).title("mock title").releaseYear(2020).build()));
        Mockito.when(reviewsService.reviewsForShows(Collections.singletonList(1)))
                .thenAnswer(invocation -> Collections.singletonMap(1, Arrays.asList(
                                                                                        Review.newBuilder().username("DGS User").starScore(5).submittedDate(OffsetDateTime.now()).build(),
                                                                                        Review.newBuilder().username("DGS User 2").starScore(3).submittedDate(OffsetDateTime.now()).build()
                                                                                        )
                        ));
    }

    @Test
    void shows() {
        List<String> titles = dgsQueryExecutor.executeAndExtractJsonPath(
                " { shows { title releaseYear }}",
                "data.shows[*].title");

        Assertions.assertThat(titles).contains("mock title");
    }

    @Test
    void showsWithException() {
        Mockito.when(showsService.shows()).thenThrow(new RuntimeException("nothing to see here"));

        ExecutionResult result = dgsQueryExecutor.execute(
                " { shows { title releaseYear }}");

        assertThat(result.getErrors()).isNotEmpty();
        assertThat(result.getErrors().get(0).getMessage()).isEqualTo("java.lang.RuntimeException: nothing to see here");
    }

    @Test
    void showsWithQueryApi() {
        GraphQLQueryRequest graphQLQueryRequest = new GraphQLQueryRequest(ShowsGraphQLQuery.newRequest().titleFilter("").build(), new ShowsProjectionRoot().title());
        List<String> titles = dgsQueryExecutor.executeAndExtractJsonPath(graphQLQueryRequest.serialize(), "data.shows[*].title");
        assertThat(titles).contains("mock title");
    }

    @Test
    void showWithReviews() {
        GraphQLQueryRequest graphQLQueryRequest = new GraphQLQueryRequest(ShowsGraphQLQuery.newRequest().titleFilter("").build(),
                new ShowsProjectionRoot()
                        .title()
                        .reviews()
                        .username()
                        .starScore());

        List<Show> shows = dgsQueryExecutor.executeAndExtractJsonPathAsObject(
                graphQLQueryRequest.serialize(),
                "data.shows[*]",
                new TypeRef<List<Show>>() {
                });

        assertThat(shows.size()).isEqualTo(1);
        assertThat(shows.get(0).getReviews().size()).isEqualTo(2);
    }

    @Test
    void addReviewMutation() {
        GraphQLQueryRequest graphQLQueryRequest = new GraphQLQueryRequest(
                AddReviewGraphQLQuery.newRequest()
                        .review(SubmittedReview.newBuilder()
                                .showid(1)
                                .username("testuser")
                                .startScore(5).build())
                        .build(),
                new AddReviewProjectionRoot().username().starScore());

        ExecutionResult executionResult = dgsQueryExecutor.execute(graphQLQueryRequest.serialize());
        assertThat(executionResult.getErrors()).isEmpty();

        verify(reviewsService).reviewsForShows(1);
    }



}
