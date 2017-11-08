package com.emprovise.api.google.maps;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class LocationApiTest {

    @Test
    public void placeAutocomplete() throws Exception {

        LocationApi distanceApi = new LocationApi("GOOGLE_API_KEY");
        List<String> results = distanceApi.placeAutocomplete("Perth, Australia");

        assertFalse(results.isEmpty());
        assertEquals(5, results.size());
        System.out.println(results);
    }
}