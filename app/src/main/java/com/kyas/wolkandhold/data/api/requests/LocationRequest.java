package com.kyas.wolkandhold.data.api.requests;

public class LocationRequest {
    double lat;
    double lon;

    public LocationRequest(double lat, double lon) {
        this.lat = lat;
        this.lon = lon;
    }
}
