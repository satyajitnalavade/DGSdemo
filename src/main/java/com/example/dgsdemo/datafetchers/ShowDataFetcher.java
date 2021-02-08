package com.example.dgsdemo.datafetchers;


import com.example.dgsdemo.generated.types.Show;
import com.example.dgsdemo.services.ShowService;
import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsData;
import graphql.schema.DataFetchingEnvironment;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author satya
 */
@DgsComponent
public class ShowDataFetcher {
    private final ShowService showService;

    public ShowDataFetcher(ShowService showService) {
        this.showService = showService;
    }

    @DgsData(
            parentType = "Query",
            field = "shows"
    )
    public List<Show> getShows(DataFetchingEnvironment dataFetchingEnvironment) {

        String titleFilter = dataFetchingEnvironment.getArgument("titleFilter");
        if (titleFilter == null){
            return showService.shows();
        }
        return showService.shows().stream().filter(show -> show.getTitle().contains(titleFilter)).collect(Collectors.toList());
    }
}
