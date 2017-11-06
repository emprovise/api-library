package com.emprovise.api.socialmedia.twitter;

import org.junit.Assert;
import org.junit.Test;
import twitter4j.Status;

import java.util.List;

public class TwitterApiTest {

    @Test
    public void searchTweets() throws Exception {

        TwitterApi api = TwitterApi.getTwitterApi("CONSUMER_KEY", "CONSUMER_SECRET");
        List<Status> allStatuses = api.searchTweets("Steve");

        Assert.assertNotNull(allStatuses);
        Assert.assertTrue(!allStatuses.isEmpty());

        for (Status status : allStatuses) {
            System.out.println(status);
        }
    }
}