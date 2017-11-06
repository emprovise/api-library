package com.emprovise.api.google.maps;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class DistanceApiTest {

    @Test
    public void getDistance() throws Exception {

        DistanceApi distanceApi = new DistanceApi("GOOGLE_API_KEY");
        JsonObject object = distanceApi.getDistance("Perth, Australia", "Sydney, Australia");

        JsonElement distance = object.get("distance");
        assertEquals("3933958", distance.getAsString());

        JsonElement duration = object.get("duration");
        assertEquals("147311", duration.getAsString());
    }
}