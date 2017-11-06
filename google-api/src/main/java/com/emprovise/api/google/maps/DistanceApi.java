package com.emprovise.api.google.maps;

import com.google.gson.JsonObject;
import com.google.maps.DistanceMatrixApi;
import com.google.maps.DistanceMatrixApiRequest;
import com.google.maps.model.*;

public class DistanceApi extends MapApi {

    public DistanceApi(String apiKey) {
        super(apiKey);
    }

    /**
     * https://developers.google.com/maps/documentation/distance-matrix/intro
     * https://maps.googleapis.com/maps/api/distancematrix/json?origins=Seattle&destinations=San+Francisco&mode=car&language=en-US&key=API_KEY
     *
     * @param from
     * @param to
     * @return
     * @throws Exception
     */
    public JsonObject getDistance(String from, String to) throws Exception {
        DistanceMatrixApiRequest req = DistanceMatrixApi.newRequest(getApiContext())
                                                        .origins(from)
                                                        .destinations(to)
                                                        .mode(TravelMode.DRIVING)
                                                        .units(Unit.METRIC);
        DistanceMatrix matrix = req.await();

        if(matrix.rows.length > 0) {
            DistanceMatrixRow row = matrix.rows[0];

            if(row.elements.length > 0) {
                DistanceMatrixElement element = row.elements[0];

                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("distance", element.distance.inMeters);
                jsonObject.addProperty("duration", element.duration.inSeconds);
                return jsonObject;
            }
        }

        return null;
    }
}
