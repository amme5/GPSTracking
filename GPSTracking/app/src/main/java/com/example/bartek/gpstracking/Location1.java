package com.example.bartek.gpstracking;

/**
 * Created by Bartek on 2017-04-18.
 */

public class Location1 {

    String locationID;
    String latitude;
    String longitude;

    public Location1(){

    }

    public Location1(String LocationID, String Latitude, String Longitude){
        this.locationID = LocationID;
        this.latitude = Latitude;
        this.longitude = Longitude;
    }

    public String mygetLocationID() {
        return locationID;
    }

    public String mygetLatitude() {
        return latitude;
    }

    public String mygetLongitude() {
        return longitude;
    }
}

