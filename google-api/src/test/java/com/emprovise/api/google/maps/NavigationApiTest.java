package com.emprovise.api.google.maps;

import com.emprovise.api.google.maps.model.Direction;
import com.google.maps.model.LatLng;
import com.google.maps.model.TravelMode;
import org.jscience.physics.amount.Amount;
import org.junit.Test;

import javax.measure.quantity.Length;

import java.util.List;

import static javax.measure.unit.NonSI.MILE;
import static javax.measure.unit.SI.METRE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class NavigationApiTest {

    @Test
    public void getDistanceByCities() throws Exception {
        NavigationApi navigationApi = new NavigationApi("GOOGLE_API_KEY");
        long actualDistance = navigationApi.getDistance("Perth, Australia", "Sydney, Australia", TravelMode.DRIVING);
        assertEquals(3933958L, actualDistance);
    }

    @Test
    public void getDistanceByAddress() throws Exception {
        NavigationApi navigationApi = new NavigationApi("GOOGLE_API_KEY");
        long actualDistance = navigationApi.getDistance("United States Capitol, East Capitol St NE & First St SE, Washington, DC 20004",
                                                "The White House, 1600 Pennsylvania Ave NW, Washington, DC 20500", TravelMode.WALKING);

        Amount<Length> expectedDistanceMiles = Amount.valueOf(2858L, METRE).to(MILE);
        Amount<Length> actualDistanceMiles = Amount.valueOf(actualDistance, METRE).to(MILE);
        assertEquals(expectedDistanceMiles, actualDistanceMiles);
    }

    @Test
    public void getDistanceByLatLong() throws Exception {
        NavigationApi navigationApi = new NavigationApi("GOOGLE_API_KEY");

        LatLng fromLatLng = new LatLng(38.890596, -77.012142);
        LatLng toLatLng = new LatLng(38.795126, -76.886406);
        long actualDistance = navigationApi.getDistance(fromLatLng, toLatLng, TravelMode.DRIVING);
        assertEquals(29296L, actualDistance);
    }

    @Test
    public void getDirectionsByCities() throws Exception {
        NavigationApi navigationApi = new NavigationApi("GOOGLE_API_KEY");

        List<Direction> directions = navigationApi.getDirections("Statue of Liberty National Monument, New York, NY 10004",
                                    "The White House, 1600 Pennsylvania Ave NW, Washington, DC 20500", TravelMode.TRANSIT);

        assertTrue(!directions.isEmpty());
        for (Direction direction : directions) {
            System.out.println(direction);
        }

        directions = navigationApi.getDetailedDirections("Statue of Liberty National Monument, New York, NY 10004",
                                    "The White House, 1600 Pennsylvania Ave NW, Washington, DC 20500", TravelMode.TRANSIT);

        assertTrue(!directions.isEmpty());
        for (Direction direction : directions) {
            System.out.println(direction);
        }
    }
}