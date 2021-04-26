package com.example.dgsdemo.services;

import com.example.dgsdemo.generated.types.Show;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/** @author satya */
@Service
public class ShowServiceImpl implements ShowService {
  @Override
  public List<Show> shows() {
    return Stream.of(
            Show.newBuilder().id(1).title("Stranger Things").releaseYear(2016).build(),
            Show.newBuilder().id(2).title("Ozark").releaseYear(2017).build(),
            Show.newBuilder().id(3).title("The Crown").releaseYear(2016).build(),
            Show.newBuilder().id(4).title("Dead to Me").releaseYear(2019).build(),
            Show.newBuilder().id(5).title("Orange is the New Black").releaseYear(2013).build())
        .collect(Collectors.toList());
  }
}
