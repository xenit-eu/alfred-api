package eu.xenit.apix.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import org.alfresco.repo.management.subsystems.SwitchableApplicationContextFactory;
import org.alfresco.repo.search.impl.solr.SolrAdminHTTPClient;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;


public class SolrAdminClient {

    private SwitchableApplicationContextFactory searchSubSystem;
    private SolrAdminHTTPClient client;
    private String baseUrl;

    public SolrAdminClient(String baseUrl, SwitchableApplicationContextFactory searchSubSystem) {
        this.baseUrl = baseUrl;
        this.searchSubSystem = searchSubSystem;
    }

    private SolrAdminHTTPClient getHttpClient() {
        if (client == null) {
            AutowireCapableBeanFactory factory = searchSubSystem.getApplicationContext()
                    .getAutowireCapableBeanFactory();
            client = (SolrAdminHTTPClient) factory.autowire(SolrAdminHTTPClient.class, factory.AUTOWIRE_BY_TYPE, true);
            ensureBaseUrl(client, baseUrl);
            client.init();
        }
        return client;
    }

    private void ensureBaseUrl(SolrAdminHTTPClient client, String baseUrl) {
        // If we're on 5x, this method exists and *must* be called
        // If we're on 4.2, this method doesn't exist — so calling it the normal way would be a compile error
        Method[] methods = client.getClass().getMethods();
        try {
            for (Method method : methods) {
                if (method.getName().equals("setBaseUrl")) {
                    method.invoke(client, baseUrl);
                }
            }
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    public JSONObject getSolrSummaryJson() throws JSONException {
        SolrAdminHTTPClient client = getHttpClient();
        HashMap<String, String> params = new HashMap<>();
        params.put("wt", "json");
        params.put("action", "SUMMARY");
        JSONObject object = client.execute(params);
        return object.getJSONObject("Summary");
    }

    public int getLastTxId() {
        try {
            return getSolrSummaryJson().getJSONObject("alfresco").getInt("Id for last TX in index");
        } catch (JSONException e) {
            e.printStackTrace();
            return -1;
        }
    }
}
