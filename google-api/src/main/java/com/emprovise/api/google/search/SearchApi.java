package com.emprovise.api.google.search;

import com.emprovise.api.google.search.datatype.Country;
import com.emprovise.api.google.search.datatype.SearchLanguage;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.customsearch.Customsearch;
import com.google.api.services.customsearch.model.Result;
import com.google.api.services.customsearch.model.Search;

import java.io.IOException;
import java.util.List;

public class SearchApi {

    private Customsearch customsearch;
    private String apiKey;
    private String searchEngineId;

    public SearchApi(String apiKey, String searchEngineId) {
        this.apiKey = apiKey;
        this.searchEngineId = searchEngineId;

        // Set up the HTTP transport and JSON factory
        HttpTransport httpTransport = new NetHttpTransport();
        JsonFactory jsonFactory = new JacksonFactory();
        //HttpRequestInitializer initializer = (HttpRequestInitializer)new CommonGoogleClientRequestInitializer(API_KEY);
        this.customsearch = new Customsearch(httpTransport, jsonFactory, null);
    }

    public List<Result> search(String query, long startIndex, long maxResults) throws IOException {
        return search(query, null, null, startIndex, maxResults, false, SearchLanguage.English, Country.United_States);
    }

    public List<Result> searchImages(String query, long startIndex, long maxResults) throws IOException {
        return search(query, null, null, startIndex, maxResults, true, SearchLanguage.English, Country.United_States);
    }

    public List<Result> search(String query, String sortCriterion, String website, long startIndex, long maxResults, boolean imagesOnly, SearchLanguage searchLanguage, Country country) throws IOException {
        Customsearch.Cse.List list = customsearch.cse().list(query);
        list.setKey(apiKey);
        list.setCx(searchEngineId);
        // language chosen is English for search results
        if (searchLanguage != null) {
            list.setLr(searchLanguage.getLanguage());
        }

        if (country != null) {
            list.setCr(country.getCountryCode());
        }

        // set hit position of first search result
        list.setStart(startIndex);
        // set max number of search results to return
        list.setNum(maxResults);

        if (sortCriterion != null) {
            list.setSort(sortCriterion);
        }

        if (website != null) {
            list.setSiteSearch(website);
        }

        if (imagesOnly) {
            list.setSearchType("image");
            list.setImgSize("xlarge");
            list.setImgType("photo");
        }

        Search results = list.execute();
        return results.getItems();
    }
}
