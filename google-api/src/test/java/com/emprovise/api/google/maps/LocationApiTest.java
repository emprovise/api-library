package com.emprovise.api.google.maps;

import org.junit.Test;

import static org.junit.Assert.*;

public class LocationApiTest {

    @Test
    public void abc() throws Exception {

        LocationApi distanceApi = new LocationApi("AIzaSyC4zfRGxXh_woOwkbM2As2WosQ3IpRG8yg");
        String abc = distanceApi.abc("Perth, Australia");
        System.out.println(abc);
    }
}