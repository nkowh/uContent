package com.nikoyo.ucontent.uc8.security;


import com.nikoyo.ucontent.uc8.security.authentication.basic.BasicFilter;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.component.AbstractLifecycleComponent;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.ToXContent;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.json.JsonXContent;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.TermsQueryBuilder;
import org.elasticsearch.rest.RestController;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.threadpool.ThreadPool;

import java.io.IOException;
import java.util.*;

public class SecurityService extends AbstractLifecycleComponent<SecurityService> {


    private static final Map<String, Set<String>> userGroups = new HashMap<String, Set<String>>();
    private final RestController restController;
    private final Client client;
    @Inject
    private ThreadPool threadPool;

    public static Set<String> getAllprincipals(String username) {
        Set<String> allprincipals = new HashSet<String>();
        allprincipals.add(username);
        if (userGroups.containsKey(username)) {
            allprincipals.addAll(userGroups.get(username));
            return Collections.unmodifiableSet(allprincipals);
        }
        return Collections.unmodifiableSet(allprincipals);
    }


    @Inject
    public SecurityService(final Settings settings, final Client client,
                           final RestController restController) {
        super(settings);
        this.restController = restController;
        this.client = client;
    }

    @Override
    protected void doStart() throws ElasticsearchException {
        restController.registerFilter(new BasicFilter(client));
        threadPool.scheduleWithFixedDelay(new UserInGroup(), TimeValue.timeValueSeconds(30));
    }

    @Override
    protected void doStop() throws ElasticsearchException {

    }

    @Override
    protected void doClose() throws ElasticsearchException {

    }

    class UserInGroup implements Runnable {

        public void run() {
            for (String username : BasicFilter.getUsers()) {
                Set<String> groups = new HashSet<String>();
                SearchRequest searchRequest = new SearchRequest("system");
                searchRequest.types("groups");
                SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
                QueryBuilder queryBuilder = new TermsQueryBuilder("users", username);
                searchSourceBuilder.query(queryBuilder);
                searchSourceBuilder.size(Integer.MAX_VALUE);
                searchRequest.extraSource(searchSourceBuilder);
                searchRequest.listenerThreaded(false);
                SearchResponse searchResponse = client.search(searchRequest).actionGet();
                for (SearchHit searchHitFields : searchResponse.getHits().hits()) {
                    groups.add(searchHitFields.getId());
                }
                userGroups.put(username, groups);

            }


        }
    }

}
