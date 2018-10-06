package services;

import org.apache.http.HttpHost;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.MultiSearchRequest;
import org.elasticsearch.action.search.MultiSearchResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.FetchSourceContext;

import java.io.IOException;
import java.util.logging.Logger;

public class ElasticSearchService {

    public static final String FIELD_TO_SEARCH_TEXT = "text";
    public static final String FIELD_TO_SEARCH_FULL_TEXT = "full_text";
    public static final String SEARCH_TYPE = "doc";
    public static final String ELASTIC_SEARCH_HOST = "localhost";
    public static final String ELASTIC_SEARCH_PROTOCOL = "http";
    public static final int ELASTIC_SEARCH_PORT = 9200;
    public static final String NONE_FIELDS = "_none_";
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

    public long getQuantityOfDocuments(String index, String content) {

        MultiSearchRequest request = new MultiSearchRequest();

        //Busco para el camo text
        SearchRequest firstSearchRequest = new SearchRequest(index);
        firstSearchRequest.types(SEARCH_TYPE);
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(QueryBuilders.matchQuery(FIELD_TO_SEARCH_TEXT, content));
        firstSearchRequest.source(searchSourceBuilder);

        request.add(firstSearchRequest);

        //Busco por el campo full_text
        SearchRequest secondSearchRequest = new SearchRequest(index);
        firstSearchRequest.types(SEARCH_TYPE);
        searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(QueryBuilders.matchQuery(FIELD_TO_SEARCH_FULL_TEXT, content));
        secondSearchRequest.source(searchSourceBuilder);

        request.add(secondSearchRequest);


        MultiSearchResponse searchResponse = null;
        try {
            searchResponse = client.msearch(request, RequestOptions.DEFAULT);

        } catch (IOException e) {
            e.printStackTrace();
        }

        int docQuantity = 0;

        if(searchResponse != null) {

            //Para cada respuesta cuento los items
            for(MultiSearchResponse.Item item :searchResponse.getResponses()){
                if(!item.isFailure()) {
                    docQuantity += item.getResponse().getHits().totalHits;
                }
            }

        }

        return docQuantity;
    }


}
