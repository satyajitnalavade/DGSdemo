package com.example.dgsdemo.services;


import com.example.dgsdemo.generated.types.Review;
import com.example.dgsdemo.generated.types.SubmittedReview;
import com.github.javafaker.Faker;
import org.reactivestreams.Publisher;
import org.springframework.stereotype.Service;
import reactor.core.publisher.ConnectableFlux;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author satya
 */
@Service
public class DefaultReviewService implements ReviewService {

    private final ShowService showService;
    private final Map<Integer,List<Review>>reviews=new ConcurrentHashMap<>();
    private FluxSink<Review> reviewsStream;
    private ConnectableFlux<Review> reviewsPublisher;

    public DefaultReviewService(ShowService showService) {
        this.showService = showService;
    }

    @PostConstruct
    private void createReviews(){
        Faker faker = new Faker();

        showService.shows().forEach(show -> {

            List<Review>generratedReviews = IntStream.range(0,5)
                    .mapToObj(i -> {
                                        LocalDateTime date = faker.date()
                                                                    .past(300, TimeUnit.DAYS)
                                                                    .toInstant().atZone(ZoneId.systemDefault())
                                                                    .toLocalDateTime();
                                        return Review.newBuilder().submittedDate(OffsetDateTime.of(date, ZoneOffset.UTC))
                                                                    .username(faker.name().username())
                                                                    .starScore(faker.number().numberBetween(0,6)).build();
                    }).collect(Collectors.toList());
            reviews.put(show.getId(),generratedReviews);
        });

        Flux<Review> publisher = Flux.create(reviewFluxSink -> {
            reviewsStream=reviewFluxSink;
        });
        reviewsPublisher=publisher.publish();
        reviewsPublisher.connect();
    }

    @Override
    public List<Review> reviewsForShows(Integer showId) {
        return reviews.get(showId);
    }

    @Override
    public Map<Integer, List<Review>> reviewsForShows(List<Integer> showIds) {
        return reviews.entrySet().stream()
                .filter(integerListEntry -> showIds.contains(integerListEntry.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey,Map.Entry::getValue));
    }

    @Override
    public void saveReview(SubmittedReview reviewInput){
        List<Review>reviewsForShow = reviews.computeIfAbsent(reviewInput.getShowid(),integer -> new ArrayList<>());
        Review review= Review.newBuilder()
                .username(reviewInput.getUsername())
                .starScore(reviewInput.getStartScore())
                .submittedDate(OffsetDateTime.now())
                .build();
        reviewsForShow.add(review);
        reviewsStream.next(review);
    }

    @Override
    public Publisher<Review> getReviewPublisher() {
        return reviewsPublisher;
    }

}
