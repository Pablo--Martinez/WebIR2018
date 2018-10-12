package controllers;


import constants.Constants;
import org.springframework.web.bind.annotation.*;
import services.ElasticSearchService;

import java.util.HashMap;
import java.util.Map;

@RestController
public class SearchController {

    private ElasticSearchService elasticSearchService = ElasticSearchService.getInstance();

    @GetMapping("/get_tweets_count")
    public Map<String, Object> getTweetCount(@RequestParam(value="search") String search) {

        return elasticSearchService.getDocuments(Constants.INDEX,search);

    }
}
