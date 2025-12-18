package com.fitme.app.utils;

public class LocationResult {
    public double lat;
    public double lon;
    public String countryCode;
    public String countryName;
    public String regionName;
    public String specificLocation;

    public LocationResult(double lat, double lon, String countryCode, String countryName, String regionName, String specificLocation) {
        this.lat = lat;
        this.lon = lon;
        this.countryCode = countryCode;
        this.countryName = countryName;
        this.regionName = regionName;
        this.specificLocation = specificLocation;
    }
}