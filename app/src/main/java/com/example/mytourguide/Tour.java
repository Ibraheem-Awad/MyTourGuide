package com.example.mytourguide;

import java.util.ArrayList;
import java.util.List;

class Tour {
    private String tourTitle;
    private String tourID;
    public List<PlaceInList> placeList = new ArrayList<>();
    private String approval;

    public Tour() {

    }

    public Tour(String TourTitle, String TourID) {
        tourTitle = TourTitle;
        tourID = TourID;
    }

    public String getTourTitle() {
        return tourTitle;
    }

    public String getApproval() {
        return approval;
    }

    public void setApproval(String approved) {

        this.approval = approved;

    }

    public String getTourID() {
        return tourID;
    }

    public List<PlaceInList> getPlaceList() {
        return placeList;
    }

    public void setTourTitle(String tourTitle, String approved) {
        this.tourTitle = tourTitle;
    }

    public void setTourID(String tourID) {
        this.tourID = tourID;
    }
}
