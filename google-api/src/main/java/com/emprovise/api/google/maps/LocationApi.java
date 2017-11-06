package com.emprovise.api.google.maps;

import com.google.maps.PlaceAutocompleteRequest;
import com.google.maps.PlacesApi;

public class LocationApi extends MapApi {

    public LocationApi(String apiKey) {
        super(apiKey);
    }

    public String abc(String place) {
        PlaceAutocompleteRequest placeAutocompleteRequest = PlacesApi.placeAutocomplete(getApiContext(), place);
        return null;
    }
}
