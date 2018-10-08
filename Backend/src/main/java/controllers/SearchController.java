package controllers;


import constants.Constants;
import org.springframework.web.bind.annotation.*;
import services.ElasticSearchService;

import java.util.HashMap;
import java.util.Map;

@CrossOrigin
@RestController
public class SearchController {

    private ElasticSearchService elasticSearchService = ElasticSearchService.getInstance();

    @GetMapping("/get_tweets_count")
    public Map<String, Long> getTweetCount(@RequestParam(value="search") String search) throws Exception {

        Map<String, Long> response = new HashMap<>();

        response.put("count", elasticSearchService.getQuantityOfDocuments(Constants.INDEX, search));

        return response;

    }
}
