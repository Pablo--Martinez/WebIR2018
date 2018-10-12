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

	public static final double MONTEVIDEO_LATITUDE = -34.9032800;
	public static final double MONTEVIDEO_LONGITUDE = -56.1881600;
	public static final int NUMBER_OF_TWEETS = 1500;
	public static final String OR_KEYWORD = " OR ";
	public static final String EMPTY_STRING = "";
	public static final int YESTERDAY_START = -2;
	public static final int YESTERDAY_END = 1;
	public static String TWEETS_DUMP_FILE = "twitterJson.txt";
	private static String[] KEY_WORDS = {"politica","elecciones","campaña","mujica","astori","tabare","vazquez","pepe",
										 "bonomi","sendic","raul","daniel","martinez","edgardo","novic","luis","lacalle","pou",
										 "pedro", "bordaberry","jorge","larrañaga","fa","frente amplio", "pc", "partido colorado",
										 "pi","partido independiente","mpp","partido nacional","pn"};

	public static void main(String[] args) {
		if (CheckESStatus()) {
			//Obtengo nuevos tweets en caso de existir
			getYesterdayTweets();

			//Cargo los tweets en ES
			loadTweetsInES(Constants.INDEX, TWEETS_DUMP_FILE);

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

		//Trato de levantar la info desde el file
		File file = new File(fileName);

		if (!file.exists()) {
			getYesterdayTweets();
		}

		try {

			//Parseo el archivo y obtengo los tweets guardados
			ObjectMapper objectMapper = new ObjectMapper();
			Map tweetsMap;
			tweetsMap = objectMapper.readValue(file, new TypeReference<Map<String,Object>>(){});
			System.out.println(String.format("Hay %d tweets para cargar",tweetsMap.size()));

			int docsLoaded = es.loadDocumentsInIndex(index, tweetsMap);
			System.out.println(String.format("Se cargaron %d documentos en Elastic search",docsLoaded));

		} catch (IOException e){
			System.out.println(e);
		}
	}

	public static void getYesterdayTweets() {
		//Se recuperan los tweets de ayer
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.DAY_OF_MONTH, YESTERDAY_START);

		Date from = calendar.getTime();

		calendar.add(Calendar.DAY_OF_MONTH, YESTERDAY_END);

		Date to = calendar.getTime();

		Map tweetsMap;
		ObjectMapper objectMapper = new ObjectMapper();
		Gson gson = new Gson();

		try {
			List<Status> tweets = TwitterService.getTweetsByDate(getKeywordsString(), from, to, new GeoLocation(MONTEVIDEO_LATITUDE, MONTEVIDEO_LONGITUDE), NUMBER_OF_TWEETS);

			//Trato de levantar la info desde el file
			File file = new File(TWEETS_DUMP_FILE);

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

	public static String getKeywordsString() {
		String keywordsString = EMPTY_STRING;

		for(String keyword : KEY_WORDS) {
			keywordsString += keyword + OR_KEYWORD;
		}

		return keywordsString.substring(0,keywordsString.length() - 4);
	}
}
