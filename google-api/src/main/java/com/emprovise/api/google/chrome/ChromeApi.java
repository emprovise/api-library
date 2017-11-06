package com.emprovise.api.google.chrome;

import com.emprovise.api.db.sql.SQLDatabase;
import com.emprovise.api.google.chrome.dao.Cookie;
import com.emprovise.api.google.chrome.dao.Urls;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;
import com.jayway.jsonpath.spi.json.GsonJsonProvider;
import com.jayway.jsonpath.spi.json.JsonProvider;
import com.jayway.jsonpath.spi.mapper.GsonMappingProvider;
import com.jayway.jsonpath.spi.mapper.MappingProvider;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

public class ChromeApi {

    private SQLDatabase historyDB;
    private SQLDatabase cookiesDB;
    private JsonObject preferences;

    private final String WINDOWS_CHROME_PATH = String.format("C:/Users/%s/AppData/Local/Google/Chrome/User Data/Default/", System.getProperty("user.name"));

    static {
        Configuration.setDefaults(new Configuration.Defaults() {
            private final JsonProvider jsonProvider = new GsonJsonProvider();
            private final MappingProvider mappingProvider = new GsonMappingProvider();

            @Override
            public JsonProvider jsonProvider() {
                return jsonProvider;
            }

            @Override
            public MappingProvider mappingProvider() {
                return mappingProvider;
            }

            @Override
            public Set<Option> options() {
                return EnumSet.noneOf(Option.class);
            }
        });
    }

    ChromeApi() throws IOException {

        if(!new File(WINDOWS_CHROME_PATH).exists()) {
            throw new IllegalStateException(String.format("Chrome not found in path %s", WINDOWS_CHROME_PATH));
        }

        String chromeDBUrl = String.format("jdbc:sqlite:%s", WINDOWS_CHROME_PATH);
        this.historyDB = new SQLDatabase(chromeDBUrl + "History");
        this.cookiesDB = new SQLDatabase(chromeDBUrl + "Cookies");

        String preferencesString = new String(Files.readAllBytes(Paths.get(WINDOWS_CHROME_PATH + "/Preferences")));
        this.preferences = new Gson().fromJson(preferencesString, JsonObject.class);
    }

    public List<Urls> getAllHistoryUrls() {
        return historyDB.find("SELECT * FROM URLS", Urls.class);
    }

    public List<Cookie> getAllCookies() {
        return cookiesDB.find("SELECT * FROM COOKIES", Cookie.class);
    }

    public void deleteAllHistoryUrls() {
        historyDB.executeQuery("DELETE FROM URLS", null);
    }

    public void deleteAllCookies() {
        cookiesDB.executeQuery("DELETE FROM COOKIES", null);
    }

    public JsonObject getPreferences() {
        return this.preferences;
    }

    public String getPreferenceValue(String pathKey) {

        if(StringUtils.isBlank(pathKey)) {
            return null;
        }

        pathKey = pathKey.trim();

        if(!pathKey.startsWith("$.")) {
            pathKey = "$." + pathKey;
        }

        return String.valueOf(JsonPath.read(preferences, pathKey));
    }
}
