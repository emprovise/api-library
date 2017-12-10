package com.emprovise.api.flowdock;

import com.emprovise.api.flowdock.model.Message;
import com.emprovise.api.flowdock.util.HttpClient;
import com.google.gson.*;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URIBuilder;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

/**
 * @see <a href="https://www.flowdock.com/api/authentication">Authentication</a>
 * @see <a href="https://www.flowdock.com/api/flows">Flows</a>
 * @see <a href="https://www.flowdock.com/api/messages">Messages</a>
 * @see <a href="https://apps.timwhitlock.info/emoji/tables/unicode">Emoji Table</a>
 */
public class FlowdockApi {

    private HttpClient httpClient;
    private static String FLOWDOCK_HOST = "api.flowdock.com";

    public FlowdockApi(String username, String password) {
        this.httpClient = new HttpClient(username, password);
    }

    public FlowdockApi(String apiToken) {
        this.httpClient = new HttpClient(apiToken, null);
    }

    public JsonArray getFlows() throws URISyntaxException, IOException {
        URI uri = buildURI("/flows");
        return parseJson(httpClient.get(uri)).getAsJsonArray();
    }

    public JsonArray getAllFlows() throws URISyntaxException, IOException {
        URI uri = buildURI("/flows/all");
        return parseJson(httpClient.get(uri)).getAsJsonArray();
    }

    public JsonObject getFlow(String organization, String flow) throws URISyntaxException, IOException {
        URI uri = buildURI(String.format("/flows/%s/%s", organization, flow));
        return parseJson(httpClient.get(uri)).getAsJsonObject();
    }

    public JsonArray getMessages(String organization, String flow) throws URISyntaxException, IOException {
        URI uri = buildURI(String.format("/flows/%s/%s/messages", organization, flow));
        return parseJson(httpClient.get(uri)).getAsJsonArray();
    }

    public JsonArray getMessages(String organization, String flow, String threadId) throws URISyntaxException, IOException {
        URI uri = buildURI(String.format("/flows/%s/%s/threads/%s/messages", organization, flow, threadId));
        return parseJson(httpClient.get(uri)).getAsJsonArray();
    }

    public String postMessage(String organization, String flow, Message message) throws URISyntaxException, IOException {
        Gson gson = new Gson();
        String messageString = gson.toJson(message);
        URI uri = buildURI(String.format("/flows/%s/%s/messages", organization, flow));
        return httpClient.post(uri, messageString, getJsonContentHeaders());
    }

    public String postMessage(String organization, String flow, String threadId, Message message) throws URISyntaxException, IOException {
        Gson gson = new Gson();
        String messageString = gson.toJson(message);
        URI uri = buildURI(String.format("/flows/%s/%s/threads/%s/messages", organization, flow, threadId));
        return httpClient.post(uri, messageString, getJsonContentHeaders());
    }

    private URI buildURI(String path, NameValuePair... parameters) throws URISyntaxException {
        URIBuilder builder = new URIBuilder();
        builder.setScheme("https").setHost(FLOWDOCK_HOST)
                .setPath(path)
                .setParameters(parameters);
        return builder.build();
    }

    private Map<String, String> getJsonContentHeaders() {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        headers.put("Accept", "application/json");
        return headers;
    }

    private JsonElement parseJson(String response) {
        return (new JsonParser()).parse(response);
    }
}
