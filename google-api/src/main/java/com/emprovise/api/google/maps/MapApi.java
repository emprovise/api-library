package com.emprovise.api.google.maps;

import com.google.maps.GeoApiContext;

import java.util.concurrent.TimeUnit;

public class MapApi {

    private GeoApiContext apiContext;

    public MapApi(String apiKey) {
        apiContext = new GeoApiContext.Builder()
                        .queryRateLimit(1)
                        .apiKey(apiKey)
                        .connectTimeout(2, TimeUnit.SECONDS)
                        .readTimeout(2, TimeUnit.SECONDS)
                        .writeTimeout(2, TimeUnit.SECONDS)
                        .build();
    }

    public GeoApiContext getApiContext() {
        return apiContext;
    }
}
