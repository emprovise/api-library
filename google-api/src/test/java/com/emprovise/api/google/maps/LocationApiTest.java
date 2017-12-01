package com.emprovise.api.google.maps;

import com.google.maps.model.PlacesSearchResult;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

/**
 * @see <a href="https://www.programcreek.com/java-api-examples/index.php?source_dir=google-maps-services-java-master/src/test/java/com/google/maps/PlacesApiTest.java">Places Test</a>
 */
public class LocationApiTest {

    @Test
    public void placeAutocomplete() throws Exception {
        LocationApi distanceApi = new LocationApi("GOOGLE_API_KEY");
        List<String> results = distanceApi.placeAutocomplete("Perth, Australia");

        assertFalse(results.isEmpty());
        assertEquals(5, results.size());
        System.out.println(results);
    }

    @Test
    public void searchPlaces() throws Exception {
        LocationApi distanceApi = new LocationApi("GOOGLE_API_KEY");
        List<PlacesSearchResult> placesSearchResults = distanceApi.searchPlaces("Pizza in New York");

        assertFalse(placesSearchResults.isEmpty());
        System.out.println(placesSearchResults);
    }
}