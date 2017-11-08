package com.emprovise.api.google.youtube;

import com.emprovise.api.google.youtube.datatype.Order;
import com.emprovise.api.google.youtube.datatype.VideoDefinition;
import com.emprovise.api.google.youtube.datatype.VideoDuration;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.SearchResult;

import java.io.IOException;
import java.util.List;

public class YoutubeSearchApi {

    /**
     * Define Api Key variable retain's the developer's API key.
     * Set developer key from the {{ Google Cloud Console }}
     */
    private String apiKey;

    /**
     * Global instance of Youtube object which is used to make YouTube Data API requests.
     */
    private YouTube youtube;

    /**
     * Initialize a YouTube object to search for videos on YouTube.
     *
     * @param apiKey
     */
    public YoutubeSearchApi(String application, String apiKey) {
        this.apiKey = apiKey;

        // Set up the HTTP transport and JSON factory
        HttpTransport httpTransport = new NetHttpTransport();
        JsonFactory jsonFactory = new JacksonFactory();
        youtube = new YouTube.Builder(httpTransport, jsonFactory, null).setApplicationName(application).build();
    }

    public List<SearchResult> searchYoutube(String query, long maxResults) throws IOException {
        return searchYoutube(query, null, VideoDefinition.ANY, VideoDuration.ANY, maxResults);
    }

    /**
     * Search arguments https://developers.google.com/youtube/v3/docs/search/list#type
     * @param query
     * @param maxResults
     * @throws IOException
     */
    public List<SearchResult> searchYoutube(String query, Order order, VideoDefinition videoDefinition, VideoDuration videoDuration, long maxResults) throws IOException {

        // Define the API request for retrieving search results.
        YouTube.Search.List search = youtube.search().list("id,snippet");
        search.setKey(apiKey);
        search.setQ(query);

        // Restrict the search results to only include videos. See:
        // https://developers.google.com/youtube/v3/docs/search/list#type
        search.setType("video");

        if(order != null) {
            search.setOrder(order.getValue());
        }

        if(videoDefinition != null) {
            search.setVideoDefinition(videoDefinition.getValue());
        }

        if(videoDuration != null) {
            search.setVideoDuration(videoDuration.getValue());
        }

        // To increase efficiency, only retrieve the fields that the application uses.
        search.setFields("items(id/kind,id/videoId,snippet/title,snippet/thumbnails/default/url)");
        search.setMaxResults(maxResults);

        SearchListResponse searchResponse = search.execute();
        return searchResponse.getItems();
    }
}


