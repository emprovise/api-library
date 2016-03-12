### Social-Media API
---

The social media API currently has API's to access facebook using facebook4j and twitter using twitter4j. Below is the maven dependency for social-media API.

```xml
<dependency>
    <groupId>com.emprovise.api</groupId>
    <artifactId>socialmedia-api</artifactId>
    <version>1.0</version>
</dependency>
```

The social media API has [sparkjava server] (http://sparkjava.com/documentation.html) configured in order to capture the redirects for OAuth tokens.

To use the Facebook social media API, register your app on https://developers.facebook.com, and then pass both your App Id and App Secret to the API as in the below example. Also in order to capture the token set the callback URL in "**Valid OAuth redirect URIs**" to "http://127.0.0.1:4567/callback" by navigating from "**Settings**" menu, "**Advanced**" tab and "**Client OAuth Settings**" section of facebook developer. Once the callback service captures the token, it generates the AccessToken and saves the AppId, AppSecret and generated AccessToken in facebook4j.properties file in target to reuse. Every 15 minutes, another call is made to facebook api to fetch a new token to update the access token. Below is the sample example of calling the facebook API.

```java
 FacebookApi api = getFacebookApi("YOUR_APP_ID", "YOUR_APP_SECRET");
 List<Post> posts = api.searchPosts("obama");
 ```
 
Similar to facebook API, twitter API also requires to register your app on https://apps.twitter.com and set the callback URL as http://127.0.0.1:4567/callback. Twitter API takes Consumer Key and Consumer Secret for your App. It calls the twitter service to authenticate and is redirected to the callback service to fetch the oauth verification code. The verification code is used to generate the access token and access secret which are save in twitter4j.properties file in target along with consumer key and consumer secret. Every 15 minutes, another call is made to twitter service to fetch a new oauth verification to update the access token and secret. Below is the sample example of calling the twitter API.
 
 ```java
 TwitterApi api = TwitterApi.getTwitterApi("YOUR_APP_ID", "YOUR_APP_SECRET");
 List<Status> allStatuses = api.searchTweets("Steve");
 for (Status status : allStatuses) {
    System.out.println(status);
 }
 ```
