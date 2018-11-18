package services;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;
import org.apache.http.HttpHost;
import org.elasticsearch.action.admin.cluster.health.ClusterHealthRequest;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.MultiSearchRequest;
import org.elasticsearch.action.search.MultiSearchResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.unit.Fuzziness;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.FetchSourceContext;

public class ElasticSearchService {

    public static final String FIELD_TO_SEARCH_TEXT = "text";
    public static final String FIELD_TO_SEARCH_FULL_TEXT = "full_text";
    public static final String SEARCH_TYPE = "doc";
    public static final String ELASTIC_SEARCH_HOST = "localhost";
    public static final String ELASTIC_SEARCH_PROTOCOL = "http";
    public static final int ELASTIC_SEARCH_PORT = 9200;
    public static final String NONE_FIELDS = "_none_";
    public static final String DATE_FORMAT_FROM = "MMM d, yyyy h:mm:ss a";
    public static final String DATE_FORMAT_TO = "dd/MM/YYYY";
    public static final String CREATED_AT = "createdAt";
    public static final String SEARCH = "search";
    public static final String TOTAL_COUNT = "total_count";
    public static final String RESULTS = "results";
    private Logger LOGGER = Logger.getLogger("elasticSearchService");
    private static ElasticSearchService INSTANCE = null;
    private RestHighLevelClient client;

    private ElasticSearchService() {
        client = new RestHighLevelClient(
                RestClient.builder(
                        new HttpHost(ELASTIC_SEARCH_HOST, ELASTIC_SEARCH_PORT, ELASTIC_SEARCH_PROTOCOL)));
    }

    public static ElasticSearchService getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ElasticSearchService();
        }

        return INSTANCE;
    }

    public boolean checkESStatus() throws IOException {
        ClusterHealthRequest request = new ClusterHealthRequest();
        client.cluster().health(request, RequestOptions.DEFAULT);

        return true;
    }

    public boolean existsDocument(String index, String id) {
        GetRequest getRequest = new GetRequest(index, SEARCH_TYPE, id);
        getRequest.fetchSourceContext(new FetchSourceContext(false));
        getRequest.storedFields(NONE_FIELDS);

        try {
            return client.exists(getRequest, RequestOptions.DEFAULT);
        } catch (IOException e) {
            LOGGER.warning(String.format("No se pudo verificar la existencia del registro %s: %s",id,e.getMessage()));
            return false;
        }
    }

    public int loadDocumentsInIndex(String index, Map documentsToLoad) {
        AtomicInteger docsLoaded = new AtomicInteger(0);

        documentsToLoad.forEach( (id, doc) -> {
            //Verifico que no exista el id en ES
            if(!this.existsDocument(index, id.toString())) {
                if(this.putDocument(index, id.toString(),doc.toString())) {
                    docsLoaded.getAndIncrement();
                    System.out.println(String.format("Se ingreso el indice %s",id));
                } else {
                    System.out.println(String.format("Ocurrio un error al ingresar el indice %s",id));
                }
            }

        });

        return docsLoaded.get();
    }

    public boolean putDocument(String index, String id, String content){
        IndexRequest request = new IndexRequest(index, SEARCH_TYPE, id);
        request.source(content, XContentType.JSON);

        try {
            IndexResponse response = client.index(request, RequestOptions.DEFAULT);
            return (response.status() == RestStatus.CREATED || response.status() == RestStatus.OK);
        } catch (IOException e) {
            LOGGER.warning(String.format("No se pudo ingresar el registro %s: %s",id,e.getMessage()));
            return false;
        }
    }

    public Map<String, Object> getDocuments(String index, String search) {

        int totalCount = 0;
        Map<String, Object> results = new HashMap<>();
        Map<String, Integer> countsByDate = new HashMap<>();

        MultiSearchRequest request = new MultiSearchRequest();

        //Busco para el camo text
        SearchRequest firstSearchRequest = new SearchRequest(index);
        firstSearchRequest.types(SEARCH_TYPE);
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.from(0);
        searchSourceBuilder.size(10000);
        searchSourceBuilder.query(QueryBuilders.matchQuery(FIELD_TO_SEARCH_TEXT, search).fuzziness(Fuzziness.AUTO));
        firstSearchRequest.source(searchSourceBuilder);

        request.add(firstSearchRequest);

        //Busco por el campo full_text
        SearchRequest secondSearchRequest = new SearchRequest(index);
        firstSearchRequest.types(SEARCH_TYPE);
        searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.from(0);
        searchSourceBuilder.size(10000);
        searchSourceBuilder.query(QueryBuilders.matchQuery(FIELD_TO_SEARCH_FULL_TEXT, search).fuzziness(Fuzziness.AUTO));
        secondSearchRequest.source(searchSourceBuilder);

        request.add(secondSearchRequest);



        MultiSearchResponse searchResponse = null;
        try {
            searchResponse = client.msearch(request, RequestOptions.DEFAULT);

        } catch (IOException e) {
            e.printStackTrace();
        }

        if(searchResponse != null) {

            DateFormat fromSourceFormat = new SimpleDateFormat(DATE_FORMAT_FROM);
            DateFormat toSourceFormat = new SimpleDateFormat(DATE_FORMAT_TO);

            // Almaceno todos los tweets de cada respuesta
            for(MultiSearchResponse.Item item :searchResponse.getResponses()){
                if(!item.isFailure()) {
                    SearchHits hits = item.getResponse().getHits();

                    Iterator<SearchHit> iterator = hits.iterator();

                    while (iterator.hasNext()) {
                        totalCount++;

                        SearchHit searchHit = iterator.next();

                        //Obtengo el tweet
                        Map<String, Object> tweet = searchHit.getSourceAsMap();

                        //Obtengo la fecha del tweet
                        Date tweetDate;
                        String tweetDateString;

                        try {

                            tweetDate = fromSourceFormat.parse(tweet.get(CREATED_AT).toString());
                            tweetDateString = toSourceFormat.format(tweetDate);

                            // Determino si existe o no la key de la fecha y sumo la cantidad
                            if (countsByDate.containsKey(tweetDateString)) {
                                countsByDate.put(tweetDateString,countsByDate.get(tweetDateString) + 1);
                            } else {
                                countsByDate.put(tweetDateString, 1);
                            }

                        } catch (ParseException e) {
                            e.printStackTrace();
                        }

                    }
                }
            }

        }

        results.put(SEARCH,search);
        results.put(TOTAL_COUNT,totalCount);
        results.put(RESULTS, countsByDate);

        return results;
    }
}
