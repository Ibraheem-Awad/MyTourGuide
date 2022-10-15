package com.example.mytourguide;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
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
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.annotations.Nullable;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class GuideMyTours extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawer;
    private NavigationView nav_view;

    TextView nav_header_tvDate,del_dialog_tv_title,del_dialog_tv_infoText,
            item_guide_dialog_tv_sender_name,item_guide_dialog_tv_sender_phone,item_guide_dialog_tv_title,
            item_guide_dialog_tv_info;

    CircleImageView nav_header_img;

    ConstraintLayout mainLayout;

    FirebaseAuth fAuth;
    StorageReference storageReference;

    private static boolean tourist;
    private static boolean fav_haveChildren;

    List<Tour> tours;
    List<PlaceInList> places;
    DatabaseReference guideDatabase;
    private static String userID,ListTitle,ListPlaces,ListID,ListSender,ListPhone,Approval,ListSenderMail,CurrentGuideName;
    ListView list_view_tours;

    Button del_dialog_btn_Yes, del_dialog_btn_No,item_guide_dialog_btn_yes,
            item_guide_dialog_btn_no,item_guide_dialog_btn_cancel;

    MyToursListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guide_my_tours);

        Toolbar toolbar = findViewById(R.id.guide_my_tours_toolbar);
        setSupportActionBar(toolbar);

        drawer = findViewById(R.id.guide_my_tours_drawer_layout);
        nav_view = findViewById(R.id.guide_my_tours_nav_view);

        nav_view.bringToFront();
        nav_view.setNavigationItemSelectedListener(this);

        mainLayout = findViewById(R.id.guide_my_tours_mainLayout);

        nav_view.setCheckedItem(R.id.my_tours_menu);
        list_view_tours = findViewById(R.id.guide_my_tours_list_view);

        fAuth = FirebaseAuth.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();

        places = new ArrayList<>();
        tours = new ArrayList<>();

        userID = fAuth.getCurrentUser().getUid();

        CurrentGuideName = fAuth.getCurrentUser().getDisplayName();

        guideDatabase = FirebaseDatabase.getInstance().getReference("GuidesReceivedLists").child(userID);

         adapter = new MyToursListAdapter(GuideMyTours.this, tours);
        list_view_tours.setAdapter(adapter);

        View header = nav_view.getHeaderView(0);
        nav_header_img = header.findViewById(R.id.nav_header_img);

        nav_header_tvDate = header.findViewById(R.id.nav_header_tv_date);
        Calendar calendar = Calendar.getInstance();
        String currentDate = DateFormat.getDateInstance(DateFormat.FULL).format(calendar.getTime());
        nav_header_tvDate.setText(currentDate);

        tourist = getIntent().getExtras().getBoolean("tourist");
        fav_haveChildren = getIntent().getExtras().getBoolean("showFav");

        nav_header_img.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_tour_guide));
        Log.d("mytag", "GuideMyTours - onCreate: " + tourist);
        StorageReference profileRef = storageReference.child("guides/" + fAuth.getCurrentUser().getUid() + "/profile.jpg");
        profileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Picasso.get().load(uri).into(nav_header_img);
            }
        });

        ActionBarDrawerToggle fav_toggle = new ActionBarDrawerToggle(this, drawer, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(fav_toggle);
        fav_toggle.syncState();

        list_view_tours.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Tour tour = tours.get(position);
                showGuideDialog(tour.getTourTitle(),position);
            }
        });

        list_view_tours.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                Tour tour = tours.get(position);
                Log.d("TAG_DE", "onItemLongClick: " + tour.getTourTitle());
                showDeleteDialog(tour.getTourTitle(),tour);
                return true;
            }
        });

        guideDatabase.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                if (snapshot.exists()) {
                    Tour tour = snapshot.getValue(Tour.class);
                    PlaceInList list = snapshot.getValue(PlaceInList.class);
                    if (tour != null) {
                        ListTitle = snapshot.child("listTitle").getValue(String.class);
                        ListPlaces = snapshot.child("listPlaces").getValue(String.class);
                        Approval = snapshot.child("approval").getValue(String.class);

                        tours.add(tour);
                        tour.placeList.add(list);
                        adapter.notifyDataSetChanged();
                        tour.setTourTitle(ListTitle,Approval);
                        list.setName(ListPlaces);
                        tour.setApproval(Approval);
                        Log.d("TAG231", "GuideMyTours onChildAdded: Approval: " + Approval);
                    }
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

    }

    private void showGuideDialog(String tourTitle,int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.item_guide_dialog, null);
        builder.setView(dialogView);

        item_guide_dialog_btn_yes = dialogView.findViewById(R.id.item_guide_dialog_btn_yes);
        item_guide_dialog_btn_no = dialogView.findViewById(R.id.item_guide_dialog_btn_no);
        item_guide_dialog_btn_cancel = dialogView.findViewById(R.id.item_guide_dialog_btn_cancel);
        item_guide_dialog_tv_sender_name = dialogView.findViewById(R.id.item_guide_dialog_tv_sender_name);
        item_guide_dialog_tv_sender_phone = dialogView.findViewById(R.id.item_guide_dialog_tv_sender_phone);
        item_guide_dialog_tv_title = dialogView.findViewById(R.id.item_guide_dialog_tv_title);
        item_guide_dialog_tv_info = dialogView.findViewById(R.id.item_guide_dialog_tv_info);


        guideDatabase.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @androidx.annotation.Nullable String previousChildName) {
                if (snapshot.exists()) {
                    Query query = guideDatabase.orderByChild("listTitle").equalTo(tourTitle);
                    query.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                                    Approval = childSnapshot.child("approval").getValue(String.class);

                                    ListSenderMail = childSnapshot.child("senderMail").getValue(String.class);
                                    ListSender = childSnapshot.child("senderFName").getValue(String.class);
                                    ListPhone = childSnapshot.child("senderPhone").getValue(String.class);


                                    Log.d("TAG_DE", "onDataChange: " + ListSender);
                                    Log.d("TAG_DE", "onDataChange: " + ListPhone);
                                    Log.d("TAG_DE", "onDataChange: Approval : " + Approval);

                                    item_guide_dialog_tv_sender_name.setText("Sender Name: " + ListSender);
                                    item_guide_dialog_tv_sender_phone.setText("Phone NO: " + ListPhone);

                                    if(Approval.equals("Approved")) {
                                        item_guide_dialog_tv_title.setText("You already accepted this tour, If you wish to delete the tour please long touch it");
                                        item_guide_dialog_btn_cancel.setText("OK");


                                    } else if (Approval.equals("Denied")) {
                                        item_guide_dialog_tv_title.setText("Sorry, but you already denied this tour, If you wish to delete the tour please long touch it");
                                        item_guide_dialog_btn_cancel.setText("OK");
                                    }
                                    else {
                                        item_guide_dialog_tv_info.setVisibility(View.VISIBLE);
                                        item_guide_dialog_btn_no.setVisibility(View.VISIBLE);
                                        item_guide_dialog_btn_yes.setVisibility(View.VISIBLE);
                                    }
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @androidx.annotation.Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @androidx.annotation.Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();

        item_guide_dialog_btn_yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Query query = guideDatabase.orderByChild("listTitle").equalTo(tourTitle);
                Tour tour = tours.get(position);
                query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                                ListID = childSnapshot.getKey();
                                Log.d("TAG_DE", "onDataChange: " + ListID);

                                guideDatabase.child(ListID).child("approval").setValue("Approved");
                                tour.setApproval("Approved");
                                adapter.notifyDataSetChanged();
                                sendApprovedMail();
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

                Toast.makeText(GuideMyTours.this, "Approved :)", Toast.LENGTH_SHORT).show();
                alertDialog.dismiss();

            }
        });

        item_guide_dialog_btn_no.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Query query = guideDatabase.orderByChild("listTitle").equalTo(tourTitle);
                Tour tour = tours.get(position);
                query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                                ListID = childSnapshot.getKey();
                                Log.d("TAG_DE", "onDataChange: " + ListID);
                                guideDatabase.child(ListID).child("approval").setValue("Denied");
                                tour.setApproval("Denied");
                                adapter.notifyDataSetChanged();
                                sendDeniedMail();
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
                Toast.makeText(GuideMyTours.this, "Denied", Toast.LENGTH_SHORT).show();
                alertDialog.dismiss();

            }
        });

        item_guide_dialog_btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });
    }

    private void showDeleteDialog(String tourTitle,Tour tour) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.delete_favorite_dialog, null);
        builder.setView(dialogView);

        del_dialog_btn_Yes = dialogView.findViewById(R.id.del_fav_dialog_btn_Yes);
        del_dialog_btn_No = dialogView.findViewById(R.id.del_fav_dialog_btn_No);
        del_dialog_tv_title = dialogView.findViewById(R.id.del_fav_dialog_tv_title);
        del_dialog_tv_infoText = dialogView.findViewById(R.id.del_fav_dialog_tv_infoText);

        del_dialog_tv_title.setText(R.string.del_list_dialog_tv_title);
        del_dialog_tv_infoText.setText(R.string.del_list_dialog_tv_infoText);

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
        del_dialog_btn_Yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Query query = guideDatabase.orderByChild("listTitle").equalTo(tourTitle);
                query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                                ListID = childSnapshot.getKey();
                                tours.remove(tour);
                                Log.d("TAG_DE", "onDataChange: " + ListID);
                                deletePlace(ListID);
                                adapter.notifyDataSetChanged();
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
                Toast.makeText(GuideMyTours.this, "List deleted", Toast.LENGTH_SHORT).show();
                alertDialog.dismiss();
            }
        });

        del_dialog_btn_No.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });
    }

    private void deletePlace(String listID) {
        DatabaseReference DbPlace = FirebaseDatabase.getInstance().getReference("GuidesReceivedLists").child(userID).child(listID);
        DbPlace.removeValue();
    }

    private void sendApprovedMail() {
        Log.d("TAG_FINAL", "sendDeniedMail: ListSenderMail: " + ListSenderMail);
        JavaMailAPI mailAPI = new JavaMailAPI(this,ListSenderMail,"Great News! The guide accepted your tour",
                "Hey " + ListSender + " we have great news, because "
                        + CurrentGuideName + " accepted your tour \nPlease make sure that you contact "
                        + CurrentGuideName + " for more info, here is the phone number: "
                        + ListPhone + "\nThanks for using our app " +
                        "\nMy Tour Guide Team");
        mailAPI.execute();
    }

    private void sendDeniedMail() {
        Log.d("TAG_FINAL", "sendDeniedMail: ListSenderMail: " + ListSenderMail);
        JavaMailAPI mailAPI = new JavaMailAPI(this,ListSenderMail,"We have bad news, The guide denied your tour",
                "Hey " + ListSender + " we are very sorry to tell you that "
                        + CurrentGuideName + " denied your tour " +
                        "\nPlease make sure that you contact " + CurrentGuideName + " for more info, here is the phone number: "+ ListPhone +
                        "\nThanks for using our app " +
                        "\n My Tour Guide Team");
        mailAPI.execute();
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
                accountTypeFavorites(tourist);
                break;

            case R.id.my_tours_menu:
                nav_view.setCheckedItem(R.id.my_tours_menu);
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
        Intent intent = new Intent(GuideMyTours.this, MapActivity.class);
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
        Intent intent = new Intent(GuideMyTours.this, ProfileActivity.class);
        intent.putExtra("showFav", fav_haveChildren);
        if (tourist) {
            intent.putExtra("tourist", true);
        } else {
            intent.putExtra("tourist", false);
        }
        startActivity(intent);
        finish();
    }

    public void accountTypeFavorites(boolean tourist) {
        Intent intent = new Intent(GuideMyTours.this, FavoriteActivity.class);
        intent.putExtra("showFav", fav_haveChildren);
        if (tourist) {
            intent.putExtra("tourist", true);
        } else {
            intent.putExtra("tourist", false);
        }
        startActivity(intent);
        finish();
    }
}