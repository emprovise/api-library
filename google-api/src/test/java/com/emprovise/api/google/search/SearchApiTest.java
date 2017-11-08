package com.emprovise.api.google.search;

import com.emprovise.api.google.search.datatype.Country;
import com.emprovise.api.google.search.datatype.SearchLanguage;
import com.google.api.services.customsearch.model.Result;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class SearchApiTest {

    @Test
    public void search() throws Exception {
        SearchApi gsc = new SearchApi("GOOGLE_API_KEY", "SEARCH_ENGINE_ID");
        String searchKeyWord = "paris";
        List<Result> resultList =  gsc.search(searchKeyWord, null, null, 1L, 10L, false, SearchLanguage.English, Country.United_States);

        Assert.assertNotNull(resultList);
        Assert.assertFalse(resultList.isEmpty());

        for(Result result: resultList){
            System.out.println(result.getHtmlTitle());
            System.out.println(result.getFormattedUrl());
            System.out.println(result.getHtmlSnippet());
            System.out.println("----------------------------------------");
        }
    }
}