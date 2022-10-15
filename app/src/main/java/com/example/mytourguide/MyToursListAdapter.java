package com.example.mytourguide;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

class MyToursListAdapter extends ArrayAdapter<Tour> {

    private Context mContext;
    private List<Tour> TourList;
    LayoutInflater inflater;
    public TextView tvTourTitle, tvPlace;

    public MyToursListAdapter(Context mContext, List<Tour> TourList) {
        super(mContext, R.layout.tours_item_list, TourList);
        this.mContext = mContext;
        this.TourList = TourList;
        inflater = LayoutInflater.from(mContext);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {


        View listItemView = inflater.inflate(R.layout.tours_item_list, parent, false);
        tvTourTitle = listItemView.findViewById(R.id.tour_list_tv_TourTitle);
        tvPlace = listItemView.findViewById(R.id.tour_list_tv_place);
        String places = "";
        Tour tour = TourList.get(position);
        String approval = tour.getApproval();
        if (approval != null) {
            if (approval.equals("Approved")) {
                tvTourTitle.setTextColor(Color.parseColor("#4CAF50"));
                tvPlace.setTextColor(Color.parseColor("#4CAF50"));
            } else if (approval.equals("Denied")) {
                tvTourTitle.setTextColor(Color.parseColor("#dc3545"));
                tvPlace.setTextColor(Color.parseColor("#dc3545"));
            }
        }
        if (tour.placeList != null) {
            for (int i = 0; i < tour.placeList.size(); i++) {

                if (places.equals("")) {
                    places = tour.placeList.get(i).getName();
                } else {
                    places = places + ", " + tour.placeList.get(i).getName();
                }
            }
        }
        tvPlace.setText(places);
        tvTourTitle.setText(tour.getTourTitle());

        return listItemView;
    }

    @Override
    public int getCount() {
        return TourList.size();
    }
}