package com.emprovise.api.socialmedia.facebook;

import facebook4j.Post;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class FacebookApiTest {

    @Test
    public void searchPosts() throws Exception {
        String[] permissions = new String[] {"email","manage_pages","publish_pages","publish_actions","user_posts"};
        FacebookApi fapi = FacebookApi.getFacebookApi("APP_ID", "APP_SECRET", permissions);
        List<Post> posts = fapi.searchPosts("obama");

        Assert.assertNotNull(posts);
        Assert.assertTrue(!posts.isEmpty());
        System.out.println(posts);
    }
}