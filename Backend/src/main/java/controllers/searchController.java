package controllers;


import constants.constants;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import services.elasticSearchService;

import java.util.HashMap;
import java.util.Map;

@RestController
public class searchController {

    private elasticSearchService elasticSearchService = services.elasticSearchService.getInstance();
    
    @RequestMapping(value = "/get_tweets_count", method = RequestMethod.GET)
    public Map<String, Long> getTweetCount(@RequestParam(value="search") String search) {

        Map<String, Long> response = new HashMap<>();

        response.put("count",elasticSearchService.getQuantityOfDocuments(constants.INDEX,search));

        return response;
    }
}
