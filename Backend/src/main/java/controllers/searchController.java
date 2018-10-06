package controllers;


import constants.constants;
import org.springframework.web.bind.annotation.*;
import services.elasticSearchService;

import java.util.HashMap;
import java.util.Map;

@RestController
public class searchController {

    private elasticSearchService elasticSearchService = services.elasticSearchService.getInstance();

    @GetMapping("/get_tweets_count")
    public Map<String, Long> getTweetCount(@RequestParam(value="search") String search) throws Exception {

        Map<String, Long> response = new HashMap<>();

        response.put("count", elasticSearchService.getQuantityOfDocuments(constants.INDEX, search));

        return response;

    }
}
