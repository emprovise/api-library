package com.emprovise.api.google.maps;

import com.google.maps.PlacesApi;
import com.google.maps.errors.ApiException;
import com.google.maps.model.AutocompletePrediction;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class LocationApi extends MapApi {

    public LocationApi(String apiKey) {
        super(apiKey);
    }

    public List<String> placeAutocomplete(String place) throws InterruptedException, ApiException, IOException {
        AutocompletePrediction[] predictions = PlacesApi.placeAutocomplete(getApiContext(), place).await();
        List<AutocompletePrediction> autocompletePredictions = Arrays.asList(predictions);
        return autocompletePredictions.stream()
                                      .map(result -> result.description)
                                      .collect(Collectors.toList());
    }
}
