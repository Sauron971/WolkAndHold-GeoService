package com.kyas.wolkandhold.data.api.requests;

public class LocationRequest {
    double lat;
    double lon;
    boolean isCapture;

    public LocationRequest(double lat, double lon, boolean isCapture) {
        this.lat = lat;
        this.lon = lon;
        this.isCapture = isCapture;
    }
}
