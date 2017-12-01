package com.emprovise.api.google.maps.model;

import com.google.maps.model.LatLng;
import com.google.maps.model.TransitDetails;
import com.google.maps.model.TravelMode;

import java.util.ArrayList;
import java.util.List;

public class Direction {

    private LatLng start;
    private LatLng end;
    private long distance;
    private long duration;
    private TravelMode travelMode;
    private String instructions;
    private List<LatLng> latLongRoute = new ArrayList<>();
    private TransitDetails transitDetails;

    public LatLng getStart() {
        return start;
    }

    public void setStart(LatLng start) {
        this.start = start;
    }

    public LatLng getEnd() {
        return end;
    }

    public void setEnd(LatLng end) {
        this.end = end;
    }

    /**
     * Returns Distance in meters
     * @return distance in meters
     */
    public long getDistance() {
        return distance;
    }

    public void setDistance(long distance) {
        this.distance = distance;
    }

    /**
     * Returns Duration in seconds
     * @return duration in seconds
     */
    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public TravelMode getTravelMode() {
        return travelMode;
    }

    public void setTravelMode(TravelMode travelMode) {
        this.travelMode = travelMode;
    }

    public String getInstructions() {
        return instructions;
    }

    public void setInstructions(String instructions) {
        this.instructions = instructions;
    }

    public List<LatLng> getLatLongRoute() {
        return latLongRoute;
    }

    public void addLatLongRoute(List<LatLng> latLongRoute) {
        this.latLongRoute.addAll(latLongRoute);
    }

    public void addLatLongRoute(LatLng latLongRoute) {
        this.latLongRoute.add(latLongRoute);
    }

    public TransitDetails getTransitDetails() {
        return transitDetails;
    }

    public void setTransitDetails(TransitDetails transitDetails) {
        this.transitDetails = transitDetails;
    }

    @Override
    public String toString(){
        return new com.google.gson.Gson().toJson(this);
    }
}
