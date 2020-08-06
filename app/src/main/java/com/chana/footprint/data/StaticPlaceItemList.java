package com.chana.footprint.data;

import android.content.Context;

import java.util.ArrayList;

public class StaticPlaceItemList {
    public static ArrayList<PlaceItem> placeItems = new ArrayList<>();

    public static void addPlace(Context context, PlaceItem item){
        item.onBindSharedPreference(context);
        placeItems.add(0,item);
    }

    public static void removePlace(Context context, PlaceItem item){

    }
}
