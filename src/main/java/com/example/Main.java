/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.example;

import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import services.twitterService;
import twitter4j.GeoLocation;
import twitter4j.Status;
import twitter4j.TwitterException;

/**
 *
 * @author USER
 */
public class Main {
    public static void main(String[] args) {
        Date from = new Date();
        from.setDate(20);
        
        Date to = new Date();
        to.setDate(30);
        
        List<Status> tweets;
        try {
            tweets = twitterService.getTweetsByDate("politica", from, to, new GeoLocation(-34.9032800,-56.1881600), 1000);
            
            for (Status tweet : tweets) {
                System.out.println("@" + tweet.getUser().getScreenName() + " - " + tweet.getText());
            }
            
        } catch (TwitterException ex) {
            ex.printStackTrace();
            System.out.println("Failed to search tweets: " + ex.getMessage());
            System.exit(-1);
        }
    }
}
