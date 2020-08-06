package com.chana.footprint.data;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Parcel;

import java.util.ArrayList;

public class PlaceItem{
    String id;
    String placeName;
    String placeFeel;
    Double latitude;
    Double longitude;
    String visitTime;
    ArrayList<Bitmap> images;

    public PlaceItem(){
        placeName = "";
        placeFeel = "";
        latitude = 0.0;
        longitude = 0.0;
        visitTime = "";
        id = "";
        images = new ArrayList<>();
    }

    public String getPlaceName() {
        return placeName;
    }

    public String getPlaceFeel() {
        return placeFeel;
    }

    public Double getLatitude() {
        return latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public String getVisitTime() {
        return visitTime;
    }

    public String getId() {
        return id;
    }

    public ArrayList<Bitmap> getImages() {
        return images;
    }

    public void setPlaceName(String placeName) {
        this.placeName = placeName;
    }

    public void setPlaceFeel(String placeFeel) {
        this.placeFeel = placeFeel;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public void setVisitTime(String visitTime) {
        this.visitTime = visitTime;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setImages(ArrayList<Bitmap> images) {
        this.images = images;
    }

    public void onBindSharedPreference(Context context) {
        ArrayList<String> ids = PreferenceManager.getStringArrayList(context, "placeId");
        if (!ids.contains(id)) {
            ids.add(id);
            PreferenceManager.setStringArrayList(context, "placeId", ids);
        }
        PreferenceManager.setFloat(context, id + "latitude", latitude.floatValue());
        PreferenceManager.setFloat(context, id + "longitude", longitude.floatValue());
        PreferenceManager.setString(context, id + "placeName", placeName);
        PreferenceManager.setString(context, id + "placeFeel", placeFeel);
        PreferenceManager.setString(context, id + "visitTime", visitTime);

    }
}
