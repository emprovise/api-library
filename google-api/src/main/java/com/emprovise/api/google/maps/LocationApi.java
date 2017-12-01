package com.emprovise.api.google.maps;

import com.google.maps.GeoApiContext;
import com.google.maps.PlacesApi;
import com.google.maps.model.AutocompletePrediction;
import com.google.maps.model.PlaceDetails;
import com.google.maps.model.PlacesSearchResponse;
import com.google.maps.model.PlacesSearchResult;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class LocationApi extends MapApi {

    public LocationApi(String apiKey) {
        super(apiKey);
    }

    public List<String> placeAutocomplete(String place) throws Exception {
        AutocompletePrediction[] predictions = PlacesApi.placeAutocomplete(getApiContext(), place).await();
        List<AutocompletePrediction> autocompletePredictions = Arrays.asList(predictions);
        return autocompletePredictions.stream()
                                      .map(result -> result.description)
                                      .collect(Collectors.toList());
    }

    public List<PlacesSearchResult> searchPlaces(String query) throws Exception {

        List<PlacesSearchResult> placesSearchResults = new ArrayList<>();
        GeoApiContext apiContext = getApiContext();
        PlacesSearchResponse response = PlacesApi.textSearchQuery(apiContext, query).await();
        placesSearchResults.addAll(Arrays.asList(response.results));
        Thread.sleep(3 * 1000);

        while(response.nextPageToken != null) {
            response = PlacesApi.textSearchNextPage(apiContext, response.nextPageToken).await();
            placesSearchResults.addAll(Arrays.asList(response.results));

            if(response.nextPageToken != null) {
                Thread.sleep(3 * 1000);
            }
        }

        return placesSearchResults;
    }

    public PlaceDetails placeDetails(String placeId) throws Exception {
        return PlacesApi.placeDetails(getApiContext(), placeId).await();
    }
}
