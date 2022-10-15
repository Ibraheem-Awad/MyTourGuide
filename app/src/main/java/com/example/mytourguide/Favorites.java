package com.example.mytourguide;

class Favorites {

    String placeName;
    String placeAddress;
    String placeID;

    public Favorites() {

    }
    public Favorites(String PlaceName, String PlaceAddress,String PlaceID) {
        placeName = PlaceName;
        placeAddress = PlaceAddress;
        placeID = PlaceID;
    }

    public String getPlaceName() {
        return placeName;
    }

    public String getPlaceAddress() {
        return placeAddress;
    }

    public String getPlaceID() {
        return placeID;
    }
}
