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
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import services.elasticSearchService;
import services.twitterService;
import sun.util.locale.provider.LocaleServiceProviderPool;
import twitter4j.GeoLocation;
import twitter4j.Status;
import twitter4j.TwitterException;
import twitter4j.TwitterObjectFactory;
import twitter4j.json.DataObjectFactory;

/**
 *
 * @author USER
 */
public class Main {
    public static void main(String[] args) {
        //Cargo los tweets de ayer
        //getYesterdayTweets();

        //Cargo los tweets en ES
        loadTweetsInES("twitter","twitterJson.txt");

        //Obtengo para un caso particular
        getCountOfTweets("twitter", "astori");
    }

    public static void loadTweetsInES(String index, String fileName) {

        elasticSearchService es = elasticSearchService.getInstance();

        ObjectMapper objectMapper = new ObjectMapper();
        Map tweetsMap;

        //Trato de levantar la info desde el file
        File file = new File(fileName);

        if (!file.exists()) {
            getYesterdayTweets();
        }

        try {
            //Parseo el archivo y obtengo los tweets guardados
            tweetsMap = objectMapper.readValue(file, new TypeReference<Map<String,Object>>(){});

            tweetsMap.forEach( (id, tweet) -> {
                //Verifico que no exista el id en ES
                if(!es.existsDocument(index, "doc", id.toString())) {
                    if(es.putDocument(index,"doc", id.toString(),tweet.toString())) {
                        System.out.println(String.format("Se ingreso el indice %s",id));
                    } else {
                        System.out.println(String.format("Ocurrio un error al ingresar el indice %s",id));
                    }
                }

            });
        } catch (IOException e){
            System.out.println(e);
        }

    }

    public static void getCountOfTweets(String index, String content) {
        System.out.println("Registros encontrado: " + elasticSearchService.getInstance().getQuantityOfDocuments(index,content));
    }

    public static void getYesterdayTweets() {
        //Se recuperan los tweets de ayer
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH,-2);

        Date from = calendar.getTime();

        calendar.add(Calendar.DAY_OF_MONTH,1);

        Date to = calendar.getTime();

        Map tweetsMap;
        ObjectMapper objectMapper = new ObjectMapper();
        Gson gson = new Gson();

        try {
            List<Status> tweets = twitterService.getTweetsByDate("politica", from, to, new GeoLocation(-34.9032800,-56.1881600), 1500);

            //Trato de levantar la info desde el file
            File file = new File("twitterJson.txt");

            if (file.exists()) {
                //Parseo el archivo y obtengo los tweets guardados
                tweetsMap = objectMapper.readValue(file, new TypeReference<Map<String,Object>>(){});

                for(Status tweet : tweets) {
                    Long id =  tweet.getId();

                    if(!tweetsMap.containsKey(id.toString())) {
                        String json = gson.toJson(tweet);
                        tweetsMap.put(id.toString(), json);
                    }
                }

            } else {

                //Creo el mapa
                tweetsMap = new HashMap();

                for(Status tweet : tweets) {
                    Long id =  tweet.getId();
                    String json = gson.toJson(tweet);

                    if(json != null)
                        tweetsMap.put(id.toString(), json);
                }
            }

            // Guardo en el archivo y salvo
            if (tweetsMap.size() > 0) {
                file.createNewFile();
                objectMapper.writeValue(file, tweetsMap);
            }

        } catch (TwitterException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
