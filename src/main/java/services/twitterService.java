/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package services;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import twitter4j.GeoLocation;
import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;

/**
 *
 * @author USER
 */
public class twitterService {
    private static final String ACCESS_KEY = "DwaX0aV673NM0n2cnY40ndtvm";
    private static final String ACCESS_SECRET = "3coONK7a5h935HWnAAojMol9DvnbL9weX1jza3JQSwvizNfxR2";
    private static final String ACCESS_TOKEN = "2749205248-8LKVgzpuca3AV29j3UgfN5ypaHOMi5lt52aUHeT";
    private static final String ACCESS_TOKEN_SECRET = "xZj1Tt7PEdMzAcRLufuBdJRetmdibeMNCfrwRJnPy8dpJ";
    private static final String DATE_FORMAT = "yyyy-MM-dd";
    
    public static List<Status> getTweetsByDate(String search, Date since, Date until, GeoLocation location, int numberOfTweets) throws TwitterException {
        ConfigurationBuilder cb = new ConfigurationBuilder();
        
        cb.setDebugEnabled(false)
          .setJSONStoreEnabled(true)
          .setOAuthConsumerKey(ACCESS_KEY)
          .setOAuthConsumerSecret(ACCESS_SECRET)
          .setOAuthAccessToken(ACCESS_TOKEN)
          .setOAuthAccessTokenSecret(ACCESS_TOKEN_SECRET);
        TwitterFactory tf = new TwitterFactory(cb.build());
        Twitter twitter = tf.getInstance();
        
        //Hay que formatear la fecha
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(DATE_FORMAT);
       
        Query query = new Query(search);
        query.geoCode(location, 25, Query.Unit.km);
        //query.setCount(100);
        query.since(simpleDateFormat.format(since));
        query.until(simpleDateFormat.format(until));
        
        long lastID = Long.MAX_VALUE;
        List<Status> tweets = new ArrayList<>();
        
        while (tweets.size () < numberOfTweets) {
            if (numberOfTweets - tweets.size() > 100)
              query.setCount(100);
            else 
              query.setCount(numberOfTweets - tweets.size());

            QueryResult result = twitter.search(query);
            tweets.addAll(result.getTweets());

            for (Status t: tweets) 
              if(t.getId() < lastID) lastID = t.getId();

            query.setMaxId(lastID-1);
          }
        
        return tweets;
    }
}
