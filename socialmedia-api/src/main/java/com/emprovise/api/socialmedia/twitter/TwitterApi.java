package com.emprovise.api.socialmedia.twitter;


import com.emprovise.api.sparkjava.CallbackRoute;
import com.emprovise.api.util.PropertiesUtil;
import twitter4j.*;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

/**
 * IMP NOTE: In order to use Twitter API, register your app on https://apps.twitter.com, pass your Consumer Key and Consumer Secret to the API.
 * Then set the callback URL as "http://127.0.0.1:4567/callback".
 */
public class TwitterApi {

    private Twitter twitter;
    private RequestToken requestToken;
    public static final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

    public TwitterApi() {
        twitter = new TwitterFactory().getInstance();
    }

    public TwitterApi(String consumerKey, String consumerSecret) {
        twitter = new TwitterFactory().getInstance();
        twitter.setOAuthConsumer(consumerKey, consumerSecret);
    }

    public TwitterApi(String consumerKey, String consumerSecret, String accessToken, String accessTokenSecret) {

        Configuration configuration =  createConfiguration(consumerKey, consumerSecret, accessToken, accessTokenSecret);
        TwitterFactory twitterFactory = new TwitterFactory(configuration);
        twitter = twitterFactory.getInstance();
    }

    /**
     * https://apps.twitter.com
     * @param consumerKey
     * @param consumerSecret
     * @param accessToken
     * @param accessTokenSecret
     * @return
     */
    public Configuration createConfiguration(String consumerKey, String consumerSecret, String accessToken, String accessTokenSecret) {
        ConfigurationBuilder confBuilder = new ConfigurationBuilder();
        confBuilder.setDebugEnabled(true);
        confBuilder.setOAuthConsumerKey(consumerKey);
        confBuilder.setOAuthConsumerSecret(consumerSecret);
        confBuilder.setOAuthAccessToken(accessToken);
        confBuilder.setOAuthAccessTokenSecret(accessTokenSecret);
        confBuilder.setHttpConnectionTimeout(500000);
        confBuilder.setJSONStoreEnabled(true);
        confBuilder.setIncludeEntitiesEnabled(true);
        return confBuilder.build();
    }

    public String getAuthenticationURL() throws TwitterException {
        twitter.setOAuthAccessToken(null);
        this.requestToken = twitter.getOAuthRequestToken();
        return requestToken.getAuthorizationURL();
    }

    public String getAuthenticationURL(String callbackURL) throws TwitterException {
        twitter.setOAuthAccessToken(null);
        this.requestToken = twitter.getOAuthRequestToken(callbackURL);
        return requestToken.getAuthenticationURL();
    }

    public void setOAuthVerifier(String verifier) throws TwitterException {
        twitter.getOAuthAccessToken(requestToken, verifier);
    }

    public void setAccessToken(String pin) throws TwitterException {
        twitter.getOAuthAccessToken(twitter.getOAuthRequestToken(), pin);
    }

    public void setAccessToken(String accessTokenString, String accesTokenSecret) throws TwitterException {
        AccessToken accessToken = new AccessToken(accessTokenString, accesTokenSecret);
        twitter.setOAuthAccessToken(accessToken);
    }

    public String getAccessToken() throws TwitterException {
        return twitter.getOAuthAccessToken().getToken();
    }

    public String getAccessTokenSecret() throws TwitterException {
        return twitter.getOAuthAccessToken().getTokenSecret();
    }

    public List<Status> getAllStatuses(String username) throws TwitterException {
        return twitter.getUserTimeline(username);
    }

    public List<Status> getAllStatuses(String username, int start, int end) throws TwitterException {
        return twitter.getUserTimeline(username, new Paging(start, end));
    }

    public List<Status> searchTweets(String searchText) throws TwitterException {
        Query query = new Query(searchText);
        QueryResult result = twitter.search(query);
        return result.getTweets();
    }

    public List<Status> searchTweets(String searchText, Date fromDate, Date toDate, int count) throws TwitterException {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        Query query = new Query(searchText);
        query.setCount(count);

        if(fromDate != null) {
            query.setSince(formatter.format(fromDate));
        }

        if(toDate != null) {
            query.setUntil(formatter.format(toDate));
        }

        QueryResult result = twitter.search(query);
        return result.getTweets();
    }

    public List<Location> getLocationTrends() throws TwitterException {
        return twitter.getAvailableTrends();
    }

    public List<Location> getLocationTrends(double latitude, double longitude) throws TwitterException {
        GeoLocation location = new GeoLocation(latitude, longitude);
        return twitter.getClosestTrends(location);
    }

    public List<User> getFollowing(String username) throws TwitterException {
        List<User> following = new ArrayList<>();
        long followingCursor = -1;
        IDs friendsIDs = twitter.getFriendsIDs(username, followingCursor);

        do {
            for (long i : friendsIDs.getIDs()) {
                System.out.println("follower ID #" + i);
                System.out.println(twitter.showUser(i).getName());
            }
        }while(friendsIDs.hasNext());
        return following;
    }

    public List<User> getFollowers(String username) throws TwitterException {

        List<User> followers = new ArrayList<>();
        final int CHUNK = 100;
        long followerCursor = -1;
        IDs followerIds;

        do {
            if(isRateLimitExceeded()) {
                break;
            }

            followerIds = twitter.getFollowersIDs(username, followerCursor);
            long[] iDs = followerIds.getIDs();

            int start = 0;
            int remaining = iDs.length - CHUNK;

            while (remaining > 0) {

                long[] part = null;

                if(remaining < CHUNK) {
                    part = new long[remaining];
                } else {
                    part = new long[CHUNK];
                }

                System.arraycopy(iDs, start, part, 0, part.length);

                ResponseList<User> responseList = twitter.lookupUsers(part);
                followers.addAll(responseList);

                for(User follower : followers) {
                    System.out.println(follower.getScreenName() + "  " + follower.getId());
                }

                start = iDs.length - remaining;
                remaining = remaining - CHUNK;
            }

        }while((followerCursor = followerIds.getNextCursor()) != 0);

        return followers;
    }

    private boolean isRateLimitExceeded() throws TwitterException {
        return !(twitter.getRateLimitStatus().get("/followers/ids").getRemaining() > 0);
    }

    public static TwitterApi getTwitterApi(String consumerKey, String consumerSecret) throws IOException, ParseException, URISyntaxException, TwitterException {

        Properties properties = PropertiesUtil.loadProperties("twitter4j.properties");
        String consumerKeyProperty = properties.getProperty("oauth.consumerKey");
        String consumerKeySecretProperty = properties.getProperty("oauth.consumerSecret");

        if(consumerKeyProperty == null || consumerKeyProperty.trim().isEmpty() || consumerKeySecretProperty == null || consumerKeySecretProperty.trim().isEmpty()) {
            properties.setProperty("oauth.consumerKey", consumerKey);
            properties.setProperty("oauth.consumerSecret", consumerSecret);
            PropertiesUtil.writeProperties("twitter4j.properties", properties);
        }

        TwitterApi api = new TwitterApi();
        long diffMinutes = 60;

        String lastUpdatedDate = properties.getProperty("lastupdate");
        if(lastUpdatedDate != null && !lastUpdatedDate.isEmpty()) {
            Date date = DATE_FORMATTER.parse(properties.getProperty("lastupdate"));
            long diff = System.currentTimeMillis() - date.getTime();
            diffMinutes = diff / (60 * 1000) % 60;
        }

        if(diffMinutes > 15) {
            CallbackRoute callbackRoute = CallbackRoute.createCallbackRoute("callback", "oauth_token", "oauth_verifier");
            String url = api.getAuthenticationURL(callbackRoute.getCallbackURL());

            if(Desktop.isDesktopSupported()) {
                Desktop.getDesktop().browse(new URI(url));
            }

            callbackRoute.startGetRoute("/callback");
            callbackRoute.waitForResponse();

            String verifier = callbackRoute.getRequestParameter("oauth_verifier");
            api.setOAuthVerifier(verifier);

            properties.setProperty("oauth.accessToken", api.getAccessToken());
            properties.setProperty("oauth.accessTokenSecret", api.getAccessTokenSecret());
            properties.setProperty("lastupdate", DATE_FORMATTER.format(new Date()));
            PropertiesUtil.writeProperties("twitter4j.properties", properties);
            CallbackRoute.stopAllRoutes();
        }

        return api;
    }
}
