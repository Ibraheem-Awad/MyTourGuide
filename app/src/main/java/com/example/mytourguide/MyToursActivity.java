package com.example.mytourguide;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
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

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.textfield.TextInputEditText;
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
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MyToursActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawer;
    private NavigationView nav_view;

    TextView nav_header_tvDate,del_dialog_tv_title,del_dialog_tv_infoText;
    CircleImageView nav_header_img;

    ConstraintLayout mainLayout;
    FloatingActionButton addList;

    FirebaseAuth fAuth;
    StorageReference storageReference;

    private static boolean tourist;
    private static boolean fav_haveChildren;
    Dialog add_tourDialog, func_tourDialog;
    Spinner select_guide_spinner;
    Button add_dialog_btn_Cancel, add_dialog_btn_Ok, del_dialog_btn_Yes, del_dialog_btn_No, func_dialog_btn_cancel, func_dialog_btn_confirm;
    TextInputEditText dialog_tie_tourName;
    DatabaseReference userDatabase, placesRef, guideListDatabase, sendToGuideDB,phoneDB,emailDB;
    private static String userID, SelectedGuide, ListID, receiverID, clickedTourTitle, senderName,clickedTourPlaces,senderPhone,senderMail;
    private static boolean CheckedPlaces = false, CheckedPhone = false;
    ListView list_view_tours;
    List<Tour> tours;
    ValueEventListener listener;
    ArrayList<String> GuideList;
    MyToursListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_tours);

        Toolbar toolbar = findViewById(R.id.my_tours_toolbar);
        setSupportActionBar(toolbar);

        drawer = findViewById(R.id.my_tours_drawer_layout);
        nav_view = findViewById(R.id.my_tours_nav_view);

        nav_view.bringToFront();
        nav_view.setNavigationItemSelectedListener(this);

        mainLayout = findViewById(R.id.my_tours_mainLayout);
        addList = findViewById(R.id.my_tours_fab_add);
        list_view_tours = findViewById(R.id.my_tours_list_view);

        nav_view.setCheckedItem(R.id.my_tours_menu);

        fAuth = FirebaseAuth.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();

        tours = new ArrayList<>();
        GuideList = new ArrayList<>();

        adapter = new MyToursListAdapter(MyToursActivity.this, tours);
        list_view_tours.setAdapter(adapter);

        View header = nav_view.getHeaderView(0);
        nav_header_img = header.findViewById(R.id.nav_header_img);

        nav_header_tvDate = header.findViewById(R.id.nav_header_tv_date);
        Calendar calendar = Calendar.getInstance();
        String currentDate = DateFormat.getDateInstance(DateFormat.FULL).format(calendar.getTime());
        nav_header_tvDate.setText(currentDate);

        userID = fAuth.getCurrentUser().getUid();
        senderName = fAuth.getCurrentUser().getDisplayName();
        Log.d("mytag", "onCreate: " + senderName);
        userDatabase = FirebaseDatabase.getInstance().getReference("UsersToursList").child(userID);
        guideListDatabase = FirebaseDatabase.getInstance().getReference("Guides");
        sendToGuideDB = FirebaseDatabase.getInstance().getReference("GuidesReceivedLists");
        phoneDB = FirebaseDatabase.getInstance().getReference("Users").child(userID).child("phone");
        emailDB = FirebaseDatabase.getInstance().getReference("Users").child(userID).child("email");


        tourist = getIntent().getExtras().getBoolean("tourist");
        fav_haveChildren = getIntent().getExtras().getBoolean("showFav");


        nav_header_img.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_tourist));
        Log.d("mytag", "MyToursActivity - onCreate: " + tourist);
        StorageReference profileRef = storageReference.child("users/" + fAuth.getCurrentUser().getUid() + "/profile.jpg");
        profileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Picasso.get().load(uri).into(nav_header_img);
            }
        });

        if (savedInstanceState == null) {
            nav_view.setCheckedItem(R.id.my_tours_menu);
        }

        ActionBarDrawerToggle fav_toggle = new ActionBarDrawerToggle(this, drawer, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(fav_toggle);
        fav_toggle.syncState();

        addList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                add_tourDialog.show();
            }
        });

        phoneDB.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                senderPhone = snapshot.getValue().toString();
                Log.d("TAG_99", "onDataChange: MyToursActivity Guide senderPhone :" + senderPhone);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        emailDB.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                senderMail = snapshot.getValue().toString();
                Log.d("TAG_99", "onDataChange: MyToursActivity Guide senderMail :" + senderMail);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        list_view_tours.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                getAllGuidesFromDB();
                func_tourDialog.show();
                Tour tour = tours.get(position);
                clickedTourPlaces="";
                if(tour.placeList != null) {
                    for(int i=0; i< tour.placeList.size();i++) {
                        if(clickedTourPlaces.equals("")) {
                            clickedTourPlaces = tour.placeList.get(i).getName();
                        }
                        else {
                            clickedTourPlaces = clickedTourPlaces + ", " + tour.placeList.get(i).getName();
                        }
                    }
                }
                clickedTourTitle = tour.getTourTitle();
                Log.d("mytag", "onItemLongClick: TITLE " + clickedTourTitle);
                Log.d("mytag", "onItemLongClick: PLACES : " + clickedTourPlaces);
            }
        });

        list_view_tours.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                Tour tour = tours.get(position);
                showDeleteDialog(tour.getTourID(),tour);
                return true;
            }
        });


        userDatabase.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                if (snapshot.exists()) {
                    Log.d("mytag", "MyToursActivity onChildAdded: Database Snapshot: " + snapshot);
                    Tour tour = snapshot.getValue(Tour.class);
                    if (tour != null) {
                        tours.add(tour);
                        adapter.notifyDataSetChanged();
                        ListID = snapshot.getKey();
                        Log.d("mytag", "MyToursActivity onChildAdded: Database Lists ID: " + ListID);
                        placesRef = userDatabase.child(ListID).child("places");
                        placesRef.addChildEventListener(new ChildEventListener() {
                            @Override
                            public void onChildAdded(@NonNull DataSnapshot PlaceSnapshot, @Nullable String previousChildName) {
                                if (PlaceSnapshot.exists()) {
                                    PlaceInList place = PlaceSnapshot.getValue(PlaceInList.class);
                                    Log.d("mytag", "MyToursActivity onChildAdded: Database Places ID: " + PlaceSnapshot.getKey());
                                    if (place != null) {
                                        tour.placeList.add(place);
                                        adapter.notifyDataSetChanged();
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


        // ********************************************** ADD TOUR LIST DIALOG **********************************************************
        add_tourDialog = new Dialog(MyToursActivity.this);
        add_tourDialog.setContentView(R.layout.add_tour_list_dialog);
        add_tourDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        add_tourDialog.setCancelable(false);

        add_dialog_btn_Cancel = add_tourDialog.findViewById(R.id.add_tour_dialog_btn_Cancel);
        add_dialog_btn_Ok = add_tourDialog.findViewById(R.id.add_tour_dialog_btn_Ok);
        dialog_tie_tourName = add_tourDialog.findViewById(R.id.add_tour_dialog_tie_tourName);

        add_dialog_btn_Ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String tourTitle = dialog_tie_tourName.getText().toString();
                if (!tourTitle.equals("")) {
                    String id = userDatabase.push().getKey();
                    Tour tour = new Tour(tourTitle, id);
                    userDatabase.child(id).setValue(tour).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(MyToursActivity.this, "List  successfully created", Toast.LENGTH_SHORT).show();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(MyToursActivity.this, "List was not made " + e.getMessage(), Toast.LENGTH_LONG).show();
                            add_tourDialog.dismiss();
                        }
                    });
                    add_tourDialog.dismiss();
                } else {
                    Toast.makeText(getApplicationContext(), "You should give your tour a name", Toast.LENGTH_SHORT).show();
                }
            }
        });

        add_dialog_btn_Cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                add_tourDialog.dismiss();
            }
        });

        // ********************************************** CHOSE GUIDE DIALOG **********************************************************
        func_tourDialog = new Dialog(MyToursActivity.this);
        func_tourDialog.setContentView(R.layout.functions_tour_dialog);
        func_tourDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        func_tourDialog.setCancelable(false);

        func_dialog_btn_cancel = func_tourDialog.findViewById(R.id.func_tour_dialog_btn_cancel);
        func_dialog_btn_confirm = func_tourDialog.findViewById(R.id.func_tour_dialog_btn_confirm);
        select_guide_spinner = func_tourDialog.findViewById(R.id.func_tour_guide_dialog_spinner);

        func_dialog_btn_confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SelectedGuide = select_guide_spinner.getSelectedItem().toString();
                Log.d("mytag", "onClick: MyToursActivity Guide chosen from spinner :" + SelectedGuide);

                Query query = guideListDatabase.orderByChild("fName").equalTo(SelectedGuide);
                query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                                receiverID = childSnapshot.child("guideID").getValue(String.class);
                                Log.d("mytag", "onDataChange: MyToursActivity Guide userID :" + receiverID);

                                if(clickedTourPlaces.equals("")) {
                                    Toast.makeText(MyToursActivity.this, "Sorry, but you can't send empty list", Toast.LENGTH_SHORT).show();
                                    CheckedPlaces = false;
                                } else
                                    CheckedPlaces = true;
                                if(senderPhone.equals("NONE")) {
                                    Toast.makeText(MyToursActivity.this, "Please add a phone number to be able to send a list", Toast.LENGTH_SHORT).show();
                                    CheckedPhone = false;
                                } else
                                    CheckedPhone = true;
                                if (CheckedPlaces && CheckedPhone) {
                                    sendList();
                                    Toast.makeText(MyToursActivity.this, "List sent successfully", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
                func_tourDialog.dismiss();
            }
        });

        func_dialog_btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                func_tourDialog.dismiss();
            }
        });
    }

    private void showDeleteDialog(String tourID,Tour tour) {
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
                deletePlace(tourID);
                Toast.makeText(MyToursActivity.this, "List deleted", Toast.LENGTH_SHORT).show();
                tours.remove(tour);
                adapter.notifyDataSetChanged();
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
        DatabaseReference DbPlace = FirebaseDatabase.getInstance().getReference("UsersToursList").child(userID).child(listID);
        DbPlace.removeValue();
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
        Intent intent = new Intent(MyToursActivity.this, MapActivity.class);
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
        Intent intent = new Intent(MyToursActivity.this, ProfileActivity.class);
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
        Intent intent = new Intent(MyToursActivity.this, FavoriteActivity.class);
        intent.putExtra("showFav", fav_haveChildren);
        if (tourist) {
            intent.putExtra("tourist", true);
        } else {
            intent.putExtra("tourist", false);
        }
        startActivity(intent);
        finish();
    }

    public void getAllGuidesFromDB() {
        listener = guideListDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                GuideList.clear();
                for (DataSnapshot tourList : snapshot.getChildren()) {
                    GuideList.add(tourList.child("fName").getValue(String.class));
                }
                ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(MyToursActivity.this, R.layout.style_spinner, GuideList);
                select_guide_spinner.setAdapter(arrayAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void sendList() {
        guideListDatabase.child(receiverID);
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("senderID", userID);
        hashMap.put("receiverID", receiverID);
        hashMap.put("listTitle", clickedTourTitle + ", " + senderName);
        hashMap.put("listPlaces", clickedTourPlaces);
        hashMap.put("senderFName", senderName);
        hashMap.put("senderPhone", senderPhone);
        hashMap.put("senderMail", senderMail);
        hashMap.put("approval","Not Yet");
        sendToGuideDB.child(receiverID).push().setValue(hashMap);
    }
}