### Rally API
---

[Rally] (https://www.rallydev.com/about/what-is-rally) is the enterprise Agile management system which helps to allocate tasks to Agile teams, track work progress, manage defects and monitor the work hours burndown. Rally API allows to connect with the rally system using [RESTful services] (https://rally1.rallydev.com/slm/doc/webservice/) and read, search, update or create items for automation. Below is the maven dependency for the API. The Rally API is using the [rally-rest-api] (https://github.com/RallyTools/RallyRestToolkitForJava) to connect with rally services. It provides various methods ranging from creating user stories, defects, tags to reading user story details and updating items.

```xml
<dependency>
    <groupId>com.emprovise.api</groupId>
    <artifactId>rally-api</artifactId>
    <version>1.0</version>
</dependency>
```

The Rally API can be used to connect to rally using rally user name and password. It also provides connection through proxy server with the Proxy URL, and Proxy creditentials. Below is the example of connection rally through a proxy server.

```java
 RallyClient client = new RallyClient("RALLY_USER_NAME", "RALLY_PASSWORD", "PROXY_URL", "PROXY_USER", "PROXY_PASSWORD");
 JsonObject userStory = client.getRallyObject("US34689");
 ```
 
 Although rally can be connected using user name and password, it is recommended to use the [Rally API Key] (https://help.rallydev.com/rally-application-manager) for automation purposes. This avoids hardcoding of rally user credentials and easily managing access to applications using API keys. Below is the example of connecting rally using API keys.
 
 ```java
 RallyClient client = new RallyClient("RALLY_API_KEY");
 String description = client.getRallyDescription("US34689")
 ```
 