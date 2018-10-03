/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.example;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

import services.elasticSearchService;
import services.twitterService;
import twitter4j.GeoLocation;
import twitter4j.Status;
import twitter4j.TwitterException;
import twitter4j.json.DataObjectFactory;

/**
 *
 * @author USER
 */
public class Main {
    public static void main(String[] args) {

        //Cargo los tweets en ES
        loadTweetsInES();

        //Obtengo para un caso particular
        getCountOfTweets("twitter", "astori");
    }

    public static void loadTweetsInES() {
        elasticSearchService es = elasticSearchService.getInstance();
        Date to = new Date();

        Date from = new Date();
        from.setDate(to.getDate() - 7);

        List<Status> tweets;
        AtomicInteger docId = new AtomicInteger(0);

        try {
            //Trato de levantar la info desde el file
            File file = new File("twitterJson.txt");

            if (file.exists()) {
                try (Stream<String> stream = Files.lines(Paths.get("twitterJson.txt"))) {
                    stream.forEach(line -> {
                        if(es.putDocument("twitter","doc", docId.toString(),line)) {
                            System.out.println(String.format("Se ingreso el indice %s",docId.toString()));
                        } else {
                            System.out.println(String.format("Ocurrio un error al ingresar el indice %s",docId.toString()));
                        }
                        docId.getAndIncrement();
                    });
                } catch (IOException e){}

            } else {
                //Si no existe el file la obtengo usando la API
                tweets = twitterService.getTweetsByDate("politica", from, to, new GeoLocation(-34.9032800,-56.1881600), 1000);

                try {

                    file.createNewFile();
                    FileWriter fw = new FileWriter(file, true);

                    for (Status tweet : tweets) {
                        String json = DataObjectFactory.getRawJSON(tweet);
                        fw.write(json + "\n");
                        if(es.putDocument("twitter","doc", docId.toString(),json)){
                            System.out.println(String.format("Se ingreso el indice %s",docId.toString()));
                        } else {
                            System.out.println(String.format("Ocurrio un error al ingresar el indice %s",docId.toString()));
                        }
                        docId.getAndIncrement();
                    }

                    fw.close();

                } catch (IOException e) {

                }
            }

        } catch (TwitterException ex) {
            ex.printStackTrace();
            System.out.println("Failed to search tweets: " + ex.getMessage());
            System.exit(-1);
        }
    }

    public static void getCountOfTweets(String index, String content) {
        System.out.println("Registros encontrado: " + elasticSearchService.getInstance().getQuantityOfDocuments(index,content));
    }
}
