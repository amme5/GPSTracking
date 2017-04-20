package com.example.bartek.gpstracking2;


public class UserLocation {

    private double latitude;
    private double longitude;

    public UserLocation() {
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

/*
    public UserLocation(double Latitude, String LocationID, double Longitude){
        this.locationID = LocationID;
        this.latitude = Latitude;
        this.longitude = Longitude;
    }
/*
    public String getUserLocationID() { return locationID; }

    public double getUserLocationLatitude() { return latitude; }

    public double getUserLocationtLongitude() { return longitude; }
    */
}