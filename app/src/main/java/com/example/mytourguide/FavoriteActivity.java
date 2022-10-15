package com.example.mytourguide;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class FavoriteActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawer;
    private NavigationView nav_view;

    TextView nav_header_tvDate, tv_default;
    CircleImageView nav_header_img;

    ConstraintLayout mainLayout;

    FirebaseAuth fAuth;
    StorageReference storageReference;

    private static boolean tourist;
    private static boolean fav_haveChildren;

    ListView favListView;
    List<Favorites> favoritesList;

    DatabaseReference UserfavDbRef, GuidefavDbRef;
    Button del_fav_dialog_btn_yes, del_fav_dialog_btn_no;
    private static String userID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorite);

        Toolbar toolbar = findViewById(R.id.favorite_toolbar);
        setSupportActionBar(toolbar);

        drawer = findViewById(R.id.favorite_drawer_layout);
        nav_view = findViewById(R.id.favorite_nav_view);
        tv_default = findViewById(R.id.favorite_tv_default);

        nav_view.bringToFront();
        nav_view.setNavigationItemSelectedListener(this);

        mainLayout = findViewById(R.id.favorite_mainLayout);

        nav_view.setCheckedItem(R.id.favorites_menu);

        fAuth = FirebaseAuth.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();
        userID = fAuth.getCurrentUser().getUid();

        favListView = findViewById(R.id.fav_list_view);
        favoritesList = new ArrayList<>();

        tourist = getIntent().getExtras().getBoolean("tourist");
        fav_haveChildren = getIntent().getExtras().getBoolean("showFav");

        UserfavDbRef = FirebaseDatabase.getInstance().getReference("UsersFavoritePlaces").child(userID);
        GuidefavDbRef = FirebaseDatabase.getInstance().getReference("GuidesFavoritePlaces").child(userID);

        View header = nav_view.getHeaderView(0);
        nav_header_img = header.findViewById(R.id.nav_header_img);

        nav_header_tvDate = header.findViewById(R.id.nav_header_tv_date);
        Calendar calendar = Calendar.getInstance();
        String currentDate = DateFormat.getDateInstance(DateFormat.FULL).format(calendar.getTime());
        nav_header_tvDate.setText(currentDate);

        ActionBarDrawerToggle fav_toggle = new ActionBarDrawerToggle(this, drawer, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(fav_toggle);
        fav_toggle.syncState();

        if (fav_haveChildren) {
            if (tourist) {
                UserfavDbRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        tv_default.setVisibility(View.GONE);
                        favoritesList.clear();
                        for (DataSnapshot favoriteSnapshot : dataSnapshot.getChildren()) {
                            Favorites favorite = favoriteSnapshot.getValue(Favorites.class);
                            favoritesList.add(favorite);
                        }
                        FavoriteListAdapter listAdapter = new FavoriteListAdapter(FavoriteActivity.this, favoritesList);
                        favListView.setAdapter(listAdapter);
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });
            } else {
                GuidefavDbRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        tv_default.setVisibility(View.GONE);
                        favoritesList.clear();
                        for (DataSnapshot favoriteSnapshot : dataSnapshot.getChildren()) {
                            Favorites favorite = favoriteSnapshot.getValue(Favorites.class);
                            favoritesList.add(favorite);
                        }
                        FavoriteListAdapter listAdapter = new FavoriteListAdapter(FavoriteActivity.this, favoritesList);
                        favListView.setAdapter(listAdapter);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });
            }
        }

        if (tourist) {
            nav_header_img.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_tourist));
            Log.d("TAG2", "FavoriteActivity - onCreate: " + tourist);
            StorageReference profileRef = storageReference.child("users/" + fAuth.getCurrentUser().getUid() + "/profile.jpg");
            profileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    Picasso.get().load(uri).into(nav_header_img);
                }
            });
        } else {
            nav_header_img.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_tour_guide));
            Log.d("TAG2", "FavoriteActivity - onCreate: " + tourist);
            StorageReference profileRef = storageReference.child("guides/" + fAuth.getCurrentUser().getUid() + "/profile.jpg");
            profileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    Picasso.get().load(uri).into(nav_header_img);
                }
            });
        }

        favListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Favorites favorites = favoritesList.get(position);
                showDeleteDialog(favorites.getPlaceID());
            }
        });
    }

    private void showDeleteDialog(String placeID) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.delete_favorite_dialog, null);
        builder.setView(dialogView);

        del_fav_dialog_btn_yes = dialogView.findViewById(R.id.del_fav_dialog_btn_Yes);
        del_fav_dialog_btn_no = dialogView.findViewById(R.id.del_fav_dialog_btn_No);

        AlertDialog alertDialog = builder.create();
        alertDialog.show();

        del_fav_dialog_btn_yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deletePlace(placeID);
                alertDialog.dismiss();
            }
        });

        del_fav_dialog_btn_no.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });
    }

    private void deletePlace(String placeID) {
        if(tourist) {
            DatabaseReference DbPlace = FirebaseDatabase.getInstance().getReference("UsersFavoritePlaces").child(userID).child(placeID);
            DbPlace.removeValue();
        } else {
            DatabaseReference DbPlace = FirebaseDatabase.getInstance().getReference("GuidesFavoritePlaces").child(userID).child(placeID);
            DbPlace.removeValue();
        }
        Toast.makeText(FavoriteActivity.this, "Place deleted", Toast.LENGTH_SHORT).show();

    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.map_menu:
                accountTypeMap(tourist);
                break;

            case R.id.logout_menu:
                fAuth.signOut();
                startActivity(new Intent(getApplicationContext(), Login.class));
                finish();
                break;

            case R.id.my_profile_menu:
                accountTypeProfile(tourist);
                break;

            case R.id.favorites_menu:
                nav_view.setCheckedItem(R.id.favorites_menu);
                break;

            case R.id.my_tours_menu:
                accountTypeMyTours(tourist);
                break;
        }
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            accountTypeMap(tourist);
        }
    }

    public void accountTypeMap(boolean tourist) {
        Intent intent = new Intent(FavoriteActivity.this, MapActivity.class);
        intent.putExtra("showFav", fav_haveChildren);
        if (tourist) {
            intent.putExtra("tourist", true);
        } else {
            intent.putExtra("tourist", false);
        }
        startActivity(intent);
        finish();
    }

    public void accountTypeProfile(boolean tourist) {
        Intent intent = new Intent(FavoriteActivity.this, ProfileActivity.class);
        intent.putExtra("showFav", fav_haveChildren);
        if (tourist) {
            intent.putExtra("tourist", true);
        } else {
            intent.putExtra("tourist", false);
        }
        startActivity(intent);
        finish();
    }

    public void accountTypeMyTours(boolean tourist) {
        Intent intent;
        if (tourist) {
            intent = new Intent(FavoriteActivity.this, MyToursActivity.class);
            intent.putExtra("showFav",fav_haveChildren);
            intent.putExtra("tourist", true);
        } else {
            intent = new Intent(FavoriteActivity.this, GuideMyTours.class);
            intent.putExtra("showFav",fav_haveChildren);
            intent.putExtra("tourist", false);
        }
        startActivity(intent);
        finish();
    }
}