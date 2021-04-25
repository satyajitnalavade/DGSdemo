package com.example.dgsdemo.datafetchers;


import com.example.dgsdemo.generated.DgsConstants;
import com.example.dgsdemo.generated.types.Show;
import com.example.dgsdemo.services.ShowService;
import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsData;
import com.netflix.graphql.dgs.DgsQuery;
import com.netflix.graphql.dgs.InputArgument;
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

//    @DgsData(parentType = DgsConstants.QUERY_TYPE, field = DgsConstants.QUERY.Shows)
    @DgsQuery
    public List<Show>shows(@InputArgument("") String titleFilter) {
        //String titleFilter = dataFetchingEnvironment.getArgument("titleFilter");
        if (titleFilter == null){
            return showService.shows();
        }
        return showService.shows().stream().filter(show -> show.getTitle().contains(titleFilter)).collect(Collectors.toList());
    }
}
