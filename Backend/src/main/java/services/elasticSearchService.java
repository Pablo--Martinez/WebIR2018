package services;

import org.apache.http.HttpHost;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.unit.Fuzziness;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.search.builder.SearchSourceBuilder;

import java.io.IOException;
import java.util.logging.Logger;

public class elasticSearchService {

    public static final String FIELD_TO_SEARCH = "full_text";
    public static final String SEARCH_TYPE = "doc";
    public static final String ELASTIC_SEARCH_HOST = "localhost";
    public static final String ELASTIC_SEARCH_PROTOCOL = "http";
    public static final int ELASTIC_SEARCH_PORT = 9200;
    private Logger LOGGER = Logger.getLogger("elasticSearchService");
    private static elasticSearchService INSTANCE = null;
    private RestHighLevelClient client;

    private elasticSearchService() {
        client = new RestHighLevelClient(
                RestClient.builder(
                        new HttpHost(ELASTIC_SEARCH_HOST, ELASTIC_SEARCH_PORT, ELASTIC_SEARCH_PROTOCOL)));
    }

    public static elasticSearchService getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new elasticSearchService();
        }

        return INSTANCE;
    }

    public boolean putDocument(String index, String type, String id, String content){
        IndexRequest request = new IndexRequest(index, type, id);
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

        MatchQueryBuilder matchQueryBuilder = new MatchQueryBuilder(FIELD_TO_SEARCH, content);
        matchQueryBuilder.fuzziness(Fuzziness.AUTO);

        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        sourceBuilder.query(matchQueryBuilder);

        SearchRequest searchRequest = new SearchRequest(index);
        searchRequest.types(SEARCH_TYPE);
        searchRequest.indices(index);
        searchRequest.source(sourceBuilder);

        SearchResponse searchResponse = null;
        try {
            searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);

        } catch (IOException e) {
            e.printStackTrace();
        }

        return (searchResponse == null? 0 : searchResponse.getHits().totalHits);
    }


}
