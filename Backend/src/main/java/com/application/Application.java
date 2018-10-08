package com.application;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import constants.Constants;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import services.ElasticSearchService;
import services.TwitterService;
import twitter4j.GeoLocation;
import twitter4j.Status;
import twitter4j.TwitterException;

import java.io.File;
import java.io.IOException;
import java.util.*;

@SpringBootApplication @ComponentScan({"controllers"})
public class Application {

	public static void main(String[] args) {
		if (CheckESStatus()) {
			//Obtengo nuevos tweets en caso de existir
			getYesterdayTweets();

			//Cargo los tweets en ES
			loadTweetsInES(Constants.INDEX, "twitterJson.txt");

			SpringApplication.run(Application.class, args);
		} else {
			System.out.println("El Cluster de Elasticsearch no esta activo");
			System.exit(-1);
		}
	}

	public static boolean CheckESStatus() {
		ElasticSearchService es = ElasticSearchService.getInstance();

		try {
			return es.checkESStatus();
		} catch (IOException e) {
			return false;
		}

	}

	public static void loadTweetsInES(String index, String fileName) {

		ElasticSearchService es = ElasticSearchService.getInstance();

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

			System.out.println(String.format("Hay %d tweets para cargar",tweetsMap.size()));

			tweetsMap.forEach( (id, tweet) -> {
				//Verifico que no exista el id en ES
				if(!es.existsDocument(index, id.toString())) {
					if(es.putDocument(index, id.toString(),tweet.toString())) {
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
			List<Status> tweets = TwitterService.getTweetsByDate("politica", from, to, new GeoLocation(-34.9032800,-56.1881600), 1500);

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
