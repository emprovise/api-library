package com.emprovise.api.socialmedia.facebook;

import com.emprovise.api.sparkjava.CallbackRoute;
import com.emprovise.api.util.PropertiesUtil;
import facebook4j.*;
import facebook4j.auth.*;
import facebook4j.conf.Configuration;
import facebook4j.conf.ConfigurationBuilder;
import org.apache.commons.lang3.StringUtils;

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
import java.util.stream.Collectors;

/**
 * IMP NOTE: In order to use Facebook API, register your app on https://developers.facebook.com, pass your App Id and App Secret to the API.
 * Then set the callback URL in "Valid OAuth redirect URIs" to "http://127.0.0.1:4567/callback" by navigating from "Settings" menu, "Advanced" tab
 * and "Client OAuth Settings" section.
 *
 */
public class FacebookApi {

    private Facebook facebook;
    public static final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

    public FacebookApi() {
        facebook = new FacebookFactory().getInstance();
    }

    public FacebookApi(String appId, String appSecret) throws FacebookException {
        Configuration configuration =  createConfiguration(appId, appSecret, null);
        FacebookFactory facebookFactory = new FacebookFactory(configuration );
        facebook = facebookFactory.getInstance();
    }

    public FacebookApi(String appId, String appSecret, String token) throws FacebookException {
        this(appId, appSecret, token, null);
    }

    public FacebookApi(String[] permissions) {
        facebook = new FacebookFactory().getInstance();
        String permissionList = StringUtils.join(permissions, ',');
        facebook.setOAuthPermissions(permissionList);
    }

    public FacebookApi(String appId, String appSecret, String[] permissions) throws FacebookException {
        String permissionList = StringUtils.join(permissions, ',');
        Configuration configuration =  createConfiguration(appId, appSecret, permissionList);
        FacebookFactory facebookFactory = new FacebookFactory(configuration );
        facebook = facebookFactory.getInstance();
    }

    public FacebookApi(String appId, String appSecret, String token, String[] permissions) throws FacebookException {

        facebook = new FacebookFactory().getInstance();
        facebook.setOAuthAppId(appId, appSecret);
        AccessToken accessToken = new AccessToken(token);
        facebook.setOAuthAccessToken(accessToken);

        if(permissions != null) {
            String permissionList = StringUtils.join(permissions, ',');
            facebook.setOAuthPermissions(permissionList);
        }
    }

    public Configuration createConfiguration(String appId, String appSecret, String permissions) {
        ConfigurationBuilder confBuilder = new ConfigurationBuilder();

        confBuilder.setDebugEnabled(true);
        confBuilder.setOAuthAppId(appId);
        confBuilder.setOAuthAppSecret(appSecret);
        confBuilder.setUseSSL(true);
        confBuilder.setJSONStoreEnabled(true);
        confBuilder.setOAuthPermissions(permissions);
        //confBuilder.setOAuthAccessToken(ACCESS_TOKEN);
        //confBuilder.setRestBaseURL("https://graph.facebook.com/v2.3/");

        Configuration configuration = confBuilder.build();
        return configuration;
    }

    public String getAppAccessToken() throws FacebookException {
        return facebook.getOAuthAccessToken().getToken();
    }

    public void setAppAccessToken(String token) throws FacebookException {
        facebook.setOAuthAccessToken(new AccessToken(token));
    }

    public void setAppAccessToken() throws FacebookException {
        OAuthSupport oAuthSupport = new OAuthAuthorization(facebook.getConfiguration());
        AccessToken accessToken = oAuthSupport.getOAuthAppAccessToken();
        facebook.setOAuthAccessToken(accessToken);
    }

    public void setOAuthAccessToken(String oauthCode) throws FacebookException {
        AccessToken token = facebook.getOAuthAccessToken(oauthCode);
        facebook.setOAuthAccessToken(token);
    }

    public String getOAuthURL(String callbackURL) {
        return facebook.getOAuthAuthorizationURL(callbackURL);
    }

    public List<User> searchUsers(String searchUser) throws FacebookException {
        return facebook.searchUsers(searchUser);
    }

    public List<String> searchUserNames(String searchUser) throws FacebookException {
        ResponseList<User> results = facebook.searchUsers(searchUser);
        return results.stream().map(e -> e.getName()).collect(Collectors.<String>toList());
    }

    public void getFriends(String userId) throws FacebookException {
        ResponseList<Friend> results = facebook.getFriends(userId);
        List<String> friendList = new ArrayList<String>();

        Paging<Friend> paging1 = results.getPaging();
        for (int i = 0; i < results.size(); i++) {
            Friend f = results.get(i);
            String id = f.getId().toString();
            friendList.add(id);
            System.out.println(id);
        }
    }

    public List<Post> searchPosts(String searchPost) throws FacebookException {
        return facebook.getPosts(searchPost);
    }

    public List<Group> getGroups(String searchGroup) throws FacebookException {
        return facebook.searchGroups(searchGroup);
    }

    public void postMessage(String message) throws FacebookException {
        facebook.postStatusMessage(message);
    }

    public static FacebookApi getFacebookApi(String appId, String appSecret, String... permissions) throws IOException, FacebookException, ParseException, URISyntaxException {

        Properties properties = PropertiesUtil.loadProperties("facebook4j.properties");
        String appIdProperty = properties.getProperty("oauth.appId");
        String appSecretProperty = properties.getProperty("oauth.appSecret");
        String permissionsProperty = properties.getProperty("oauth.appSecret");

        if(appIdProperty == null || appIdProperty.trim().isEmpty() || appSecretProperty == null || appSecretProperty.trim().isEmpty() ||
           permissionsProperty == null || permissionsProperty.trim().isEmpty()) {

            properties.setProperty("oauth.appId", appId);
            properties.setProperty("oauth.appSecret", appSecret);
            PropertiesUtil.writeProperties("facebook4j.properties", properties);
        }

        long diffMinutes = 60;
        FacebookApi api = new FacebookApi(permissions);

        String lastUpdatedDate = properties.getProperty("lastupdate");
        if(lastUpdatedDate != null && !lastUpdatedDate.isEmpty()) {
            Date date = DATE_FORMATTER.parse(properties.getProperty("lastupdate"));
            long diff = System.currentTimeMillis() - date.getTime();
            diffMinutes = diff / (60 * 1000) % 60;
        }

        if(diffMinutes > 15) {
            CallbackRoute callbackRoute = CallbackRoute.createCallbackRoute("callback", "code");
            String url = api.getOAuthURL(callbackRoute.getCallbackURL());

            if(Desktop.isDesktopSupported()) {
                Desktop.getDesktop().browse(new URI(url));
            }

            callbackRoute.startGetRoute("/callback");
            callbackRoute.waitForResponse();

            String code = callbackRoute.getRequestParameter("code");
            api.setOAuthAccessToken(code);
            properties.setProperty("oauth.accessToken", api.getAppAccessToken());
            properties.setProperty("lastupdate", DATE_FORMATTER.format(new Date()));
            PropertiesUtil.writeProperties("facebook4j.properties", properties);
            CallbackRoute.stopAllRoutes();
        }

        return api;
    }
}
