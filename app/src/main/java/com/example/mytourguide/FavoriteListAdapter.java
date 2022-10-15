package com.example.mytourguide;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.util.List;

class FavoriteListAdapter extends ArrayAdapter<Favorites> {


    private Activity mContext;
    private List<Favorites> FavoritesList;

    public FavoriteListAdapter (Activity mContext,List <Favorites>  FavoritesList) {
        super(mContext,R.layout.favorite_item_list,FavoritesList);
        this.mContext = mContext;
        this.FavoritesList = FavoritesList;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        TextView tvPlaceName, tvPlaceAddress;
        LayoutInflater inflater = mContext.getLayoutInflater();
        View listItemView = inflater.inflate(R.layout.favorite_item_list,null,true);
        tvPlaceName = listItemView.findViewById(R.id.fav_list_tv_place_name);
        tvPlaceAddress = listItemView.findViewById(R.id.fav_list_tv_place_address);

        Favorites favorite = FavoritesList.get(position);

        tvPlaceName.setText(favorite.getPlaceName());
        tvPlaceAddress.setText(favorite.getPlaceAddress());

        return listItemView;
    }
}
