package com.emprovise.api.google.youtube;

import com.google.api.services.youtube.model.ResourceId;
import com.google.api.services.youtube.model.SearchResult;
import com.google.api.services.youtube.model.Thumbnail;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class YoutubeSearchApiTest {

    @Test
    public void searchYoutube() throws Exception {

        YoutubeSearchApi youtubeSearchApi = new YoutubeSearchApi("emprovise-youtube", "GOOGLE_API_KEY");
        List<SearchResult> results = youtubeSearchApi.searchYoutube("Create Youtube Video", 10L);

        Assert.assertNotNull(results);
        Assert.assertFalse(results.isEmpty());
        System.out.println("-------------------------------------------------------------\n");

        for (SearchResult result : results) {

            ResourceId resourceId = result.getId();

            // Confirm that the result represents a video. Otherwise, the item will not contain a video ID.
            if (resourceId.getKind().equals("youtube#video")) {
                Thumbnail thumbnail = result.getSnippet().getThumbnails().getDefault();

                System.out.println(" Title: " + result.getSnippet().getTitle());
                System.out.println(" Video Id" + resourceId.getVideoId());
                System.out.println(" Thumbnail: " + thumbnail.getUrl());
                System.out.println("\n-------------------------------------------------------------\n");
            }
        }
    }
}