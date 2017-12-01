package com.emprovise.api.google.maps;

import com.emprovise.api.google.maps.model.Direction;
import com.google.maps.DirectionsApi;
import com.google.maps.DistanceMatrixApi;
import com.google.maps.DistanceMatrixApiRequest;
import com.google.maps.errors.NotFoundException;
import com.google.maps.model.*;
import org.jsoup.Jsoup;

import java.util.ArrayList;
import java.util.List;

public class NavigationApi extends MapApi {

    public NavigationApi(String apiKey) {
        super(apiKey);
    }

    /**
     * https://developers.google.com/maps/documentation/distance-matrix/intro
     * https://maps.googleapis.com/maps/api/distancematrix/json?origins=Seattle&destinations=San+Francisco&language=en-US&key=API_KEY
     *
     * @param from
     * @param to
     * @return
     * @throws Exception
     */
    public long getDistance(String from, String to, TravelMode travelMode) throws Exception {
        DistanceMatrixApiRequest req = DistanceMatrixApi.newRequest(getApiContext())
                .origins(from)
                .destinations(to)
                .mode(travelMode)
                .units(Unit.METRIC);
        DistanceMatrix matrix = req.await();
        DistanceMatrixElement element = getFirstDistanceMatrixElement(matrix);
        return element.distance.inMeters;
    }

    public long getDistance(LatLng from, LatLng to, TravelMode travelMode) throws Exception {
        DistanceMatrixApiRequest req = DistanceMatrixApi.newRequest(getApiContext())
                .origins(from)
                .destinations(to)
                .mode(travelMode)
                .units(Unit.METRIC);
        DistanceMatrix matrix = req.await();
        DistanceMatrixElement element = getFirstDistanceMatrixElement(matrix);
        return element.distance.inMeters;
    }

    public long getDuration(String from, String to, TravelMode travelMode) throws Exception {
        DistanceMatrixApiRequest req = DistanceMatrixApi.newRequest(getApiContext())
                .origins(from)
                .destinations(to)
                .mode(travelMode)
                .units(Unit.METRIC);
        DistanceMatrix matrix = req.await();
        DistanceMatrixElement element = getFirstDistanceMatrixElement(matrix);
        return element.duration.inSeconds;
    }

    public long getDuration(LatLng from, LatLng to, TravelMode travelMode) throws Exception {
        DistanceMatrixApiRequest req = DistanceMatrixApi.newRequest(getApiContext())
                .origins(from)
                .destinations(to)
                .mode(travelMode)
                .units(Unit.METRIC);
        DistanceMatrix matrix = req.await();
        DistanceMatrixElement element = getFirstDistanceMatrixElement(matrix);
        return element.duration.inSeconds;
    }

    private DistanceMatrixElement getFirstDistanceMatrixElement(DistanceMatrix matrix) throws NotFoundException {
        if (matrix.rows.length > 0) {
            DistanceMatrixRow row = matrix.rows[0];

            if (row.elements.length > 0) {
                return row.elements[0];
            } else {
                throw new NotFoundException("Google Maps API response not found, no element in first row of distance matrix");
            }
        } else {
            throw new NotFoundException("Google Maps API response not found, distance matrix has no rows");
        }
    }

    public List<Direction> getDirections(String from, String to, TravelMode travelMode) throws Exception {
        return getDirections(from, to, travelMode, false);
    }

    public List<Direction> getDetailedDirections(String from, String to, TravelMode travelMode) throws Exception {
        return getDirections(from, to, travelMode, true);
    }

    private List<Direction> getDirections(String from, String to, TravelMode travelMode, boolean detailed) throws Exception {
        DirectionsResult result = DirectionsApi.newRequest(getApiContext())
                        .origin(from)
                        .destination(to)
                        .mode(travelMode)
                        .avoid(DirectionsApi.RouteRestriction.HIGHWAYS,
                               DirectionsApi.RouteRestriction.TOLLS,
                               DirectionsApi.RouteRestriction.FERRIES)
                        .units(Unit.METRIC)
                        .await();

        DirectionsRoute[] routes = result.routes;
        List<Direction> directions = new ArrayList<>();

        if(routes.length > 0) {
            DirectionsLeg[] legs = routes[0].legs;

            if(legs.length > 0) {
                for (DirectionsStep step : legs[0].steps) {
                    if(detailed) {
                        directions = getDirections(directions, step);
                    } else {
                       directions.add(getDirection(step));
                    }
                }
            }
        }

        return directions;
    }

    private List<Direction> getDirections(List<Direction> directions, DirectionsStep directionsStep) {

        if(directionsStep.steps != null && directionsStep.steps.length > 0) {
            for (DirectionsStep step : directionsStep.steps) {
                directions = getDirections(directions, step);
            }
        } else {
            directions.add(getDirection(directionsStep));
        }

        return directions;
    }

    private Direction getDirection(DirectionsStep step) {
        Direction direction = new Direction();
        direction.setStart(step.startLocation);
        direction.setEnd(step.endLocation);
        direction.setDistance(step.distance.inMeters);
        direction.setDuration(step.duration.inSeconds);
        direction.setTravelMode(step.travelMode);
        direction.addLatLongRoute(step.polyline.decodePath());
        direction.setTransitDetails(step.transitDetails);

        if(step.htmlInstructions != null) {
            direction.setInstructions(Jsoup.parse(step.htmlInstructions).text());
        }

        return direction;
    }
}
