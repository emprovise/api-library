package com.emprovise.api.google.chrome;

import com.emprovise.api.google.chrome.dao.Cookie;
import com.emprovise.api.google.chrome.dao.Urls;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class ChromeApiTest {

    @Test
    public void getAllHistoryUrls() throws Exception {
        ChromeApi chromeApi = new ChromeApi();
        List<Urls> urls = chromeApi.getAllHistoryUrls();
        Assert.assertTrue(urls.isEmpty());
    }

    @Test
    public void getAllCookies() throws Exception {
        ChromeApi chromeApi = new ChromeApi();
        List<Cookie> allCookies = chromeApi.getAllCookies();
        Assert.assertTrue(allCookies.isEmpty());
    }

    @Test
    public void getPreferenceValue() throws Exception {
        ChromeApi chromeApi = new ChromeApi();
        String preferenceValue = chromeApi.getPreferenceValue("data_reduction_lo_fi.was_used_this_session");
        Assert.assertEquals("false", preferenceValue);
    }
}