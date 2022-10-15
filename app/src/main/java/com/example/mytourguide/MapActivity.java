package com.example.mytourguide;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.api.model.AutocompleteSessionToken;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.FetchPlaceResponse;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsResponse;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.mancj.materialsearchbar.MaterialSearchBar;
import com.mancj.materialsearchbar.adapter.SuggestionsAdapter;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback, NavigationView.OnNavigationItemSelectedListener {

    private GoogleMap mMap;
    private FusedLocationProviderClient mfusedLocationProviderClient;
    private PlacesClient placesClient;
    private List<AutocompletePrediction> predictionList;

    private Location mLastKnownLocation;
    private LocationCallback locationCallback;
    private Marker mMarker;
    private TextView nav_header_tvDate;
    private MaterialSearchBar materialSearchBar;
    private View mapView;
    private ImageView mInfo,mAdd,mFavorite;
    private CircleImageView nav_header_img;
    private PlaceInfo mPlace;
    FirebaseAuth mAuth;
    FirebaseFirestore fStore;
    private static Place place;
    private static String userID;
    private final float DEFAULT_ZOOM = 18;

    private DrawerLayout drawer;
    private NavigationView nav_view;
    StorageReference storageReference;
    private static boolean tourist;
    boolean doubleBackToExitPressedOnce = false;
    private static boolean fav_haveChildren;
    DatabaseReference userDatabase,guideDatabase,myTourUserDatabase,TourListChosen_UserDB;
    String PlaceName,PlaceAddress,ListChosen;
    private static String ListID;
    Dialog add_placeDialog;
    Button add_place_dialog_btn_Cancel,add_place_dialog_btn_Ok;
    Spinner add_place_spinner;
    ValueEventListener listener;
    ArrayList<String> arrayList;
    List<Tour> places;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        drawer = findViewById(R.id.map_drawer_layout);
        nav_view = findViewById(R.id.map_nav_view);

        materialSearchBar = findViewById(R.id.searchBar);
        mInfo = findViewById(R.id.map_placeInfo);
        mAdd = findViewById(R.id.map_add);
        mFavorite = findViewById(R.id.map_favorite);

        mAuth = FirebaseAuth.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();
        fStore = FirebaseFirestore.getInstance();

        arrayList = new ArrayList<>();
        places = new ArrayList<>();

        View header = nav_view.getHeaderView(0);
        nav_header_img = header.findViewById(R.id.nav_header_img);
        nav_header_tvDate = header.findViewById(R.id.nav_header_tv_date);
        Calendar calendar = Calendar.getInstance();
        String currentDate = DateFormat.getDateInstance(DateFormat.FULL).format(calendar.getTime());
        tourist = getIntent().getExtras().getBoolean("tourist");
        fav_haveChildren = getIntent().getExtras().getBoolean("showFav");
        userID = mAuth.getCurrentUser().getUid();
        userDatabase = FirebaseDatabase.getInstance().getReference("UsersFavoritePlaces").child(userID);
        guideDatabase = FirebaseDatabase.getInstance().getReference("GuidesFavoritePlaces").child(userID);
        myTourUserDatabase = FirebaseDatabase.getInstance().getReference("UsersToursList").child(userID);


        nav_header_tvDate.setText(currentDate);
        if (tourist) {
            nav_header_img.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_tourist));
            Log.d("TAG1", "MapActivity - onCreate: " + tourist);
            StorageReference profileRef = storageReference.child("users/" + mAuth.getCurrentUser().getUid() + "/profile.jpg");
            profileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    Picasso.get().load(uri).into(nav_header_img);
                }
            });
        } else {
            nav_header_img.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_tour_guide));
            Log.d("TAG1", "MapActivity - onCreate: " + tourist);
            StorageReference profileRef = storageReference.child("guides/" + mAuth.getCurrentUser().getUid() + "/profile.jpg");
            profileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    Picasso.get().load(uri).into(nav_header_img);
                }
            });
        }

        nav_view.bringToFront();
        nav_view.setNavigationItemSelectedListener(this);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        mapView = mapFragment.getView();

        mfusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(MapActivity.this);
        Places.initialize(MapActivity.this, "AIzaSyAF_fmChP2iqsljVJjvbf26gYDFvYZ_dyk");
        placesClient = Places.createClient(this);
        final AutocompleteSessionToken token = AutocompleteSessionToken.newInstance();

        materialSearchBar.setOnSearchActionListener(new MaterialSearchBar.OnSearchActionListener() {

            @Override
            public void onSearchStateChanged(boolean enabled) {

            }

            @Override
            public void onSearchConfirmed(CharSequence text) {
                startSearch(text.toString(), true, null, false);
                materialSearchBar.showSuggestionsList();
                HideKeyboard();
            }

            @Override
            public void onButtonClicked(int buttonCode) {
                mInfo.setVisibility(View.GONE);
                mFavorite.setVisibility(View.GONE);
                mAdd.setVisibility(View.GONE);
                mMap.clear();

                if (buttonCode == MaterialSearchBar.BUTTON_NAVIGATION) {
                    drawer.openDrawer(Gravity.LEFT);
                } else if (buttonCode == MaterialSearchBar.BUTTON_BACK) {
                    materialSearchBar.clearFocus();
                    materialSearchBar.disableSearch();
                    materialSearchBar.clearSuggestions();
                }
            }
        });


        materialSearchBar.addTextChangeListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mInfo.setVisibility(View.GONE);
                mAdd.setVisibility(View.GONE);
                mFavorite.setVisibility(View.GONE);
                FindAutocompletePredictionsRequest predictionsRequest = FindAutocompletePredictionsRequest.builder()
                        .setCountry("il")
                        .setSessionToken(token)
                        .setQuery(s.toString())
                        .build();
                placesClient.findAutocompletePredictions(predictionsRequest).addOnCompleteListener(new OnCompleteListener<FindAutocompletePredictionsResponse>() {
                    @Override
                    public void onComplete(@NonNull Task<FindAutocompletePredictionsResponse> task) {
                        if (task.isSuccessful()) {
                            FindAutocompletePredictionsResponse predictionsResponse = task.getResult();
                            if (predictionsResponse != null) {
                                predictionList = predictionsResponse.getAutocompletePredictions();
                                List<String> suggestionsList = new ArrayList<>();
                                for (int i = 0; i < predictionList.size(); i++) {
                                    AutocompletePrediction prediction = predictionList.get(i);
                                    suggestionsList.add(prediction.getFullText(null).toString());
                                }
                                materialSearchBar.updateLastSuggestions(suggestionsList);
                                if (!materialSearchBar.isSuggestionsVisible()) {
                                    materialSearchBar.showSuggestionsList();
                                }
                            }
                        } else {
                            Log.i("mytag", "prediction fetching task unsuccessful");
                        }
                    }
                });
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        materialSearchBar.setSuggestionsClickListener(new SuggestionsAdapter.OnItemViewClickListener() {
            @Override
            public void OnItemClickListener(int position, View v) {
                if (position >= predictionList.size()) {
                    return;
                }
                AutocompletePrediction selectedPrediction = predictionList.get(position);
                String suggestion = materialSearchBar.getLastSuggestions().get(position).toString();
                materialSearchBar.setText(suggestion);

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        materialSearchBar.clearSuggestions();
                    }
                }, 1000);

                HideKeyboard();

                String placeId = selectedPrediction.getPlaceId();
                final List<Place.Field> placeFields = Arrays.asList(Place.Field.LAT_LNG, Place.Field.NAME, Place.Field.ID,
                        Place.Field.PHONE_NUMBER, Place.Field.WEBSITE_URI, Place.Field.ADDRESS,Place.Field.RATING,
                        Place.Field.USER_RATINGS_TOTAL);

                FetchPlaceRequest fetchPlaceRequest = FetchPlaceRequest.builder(placeId, placeFields).build();
                placesClient.fetchPlace(fetchPlaceRequest).addOnSuccessListener(new OnSuccessListener<FetchPlaceResponse>() {
                    @Override
                    public void onSuccess(FetchPlaceResponse fetchPlaceResponse) {
                        place = fetchPlaceResponse.getPlace();

                        mPlace = new PlaceInfo();
                        try {
                            mPlace.setName(place.getName());
                            Log.i("mytag", "place details name " + place.getName());

                            mPlace.setAddress(place.getAddress());
                            Log.i("mytag", "place details address " + place.getAddress());

                            mPlace.setId(place.getId());
                            Log.i("mytag", "place details address " + place.getId());

                            mPlace.setLatlng(place.getLatLng());
                            Log.i("mytag", "place details latlng " + place.getLatLng());

                            mPlace.setPhoneNumber(place.getPhoneNumber());
                            Log.i("mytag", "place details phone number " + place.getPhoneNumber());

                            mPlace.setWebsiteUri(place.getWebsiteUri());
                            Log.i("mytag", "place details website " + place.getWebsiteUri());

                            mPlace.setRating(place.getRating());
                            Log.i("mytag", "place details rating " + place.getRating());

                            mPlace.setUserRatingsTotal(place.getUserRatingsTotal());
                            Log.i("mytag", "place details total rating " + place.getUserRatingsTotal());
                        }
                        catch (NullPointerException e) {
                            Log.e("mytag", "place details: NullPointerException: " + e.getMessage());
                        }


                        LatLng latLngOfPlace = place.getLatLng();
                        if (latLngOfPlace != null) {
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLngOfPlace, DEFAULT_ZOOM));
                            Log.d("mytag", "onSuccess: MOVED THE CAMERA BOSS " + "\n mPlace " + mPlace.toString());
                            PlaceInfo(latLngOfPlace, DEFAULT_ZOOM, mPlace);

                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        if (e instanceof ApiException) {
                            ApiException apiException = (ApiException) e;
                            apiException.printStackTrace();
                            int StatusCode = apiException.getStatusCode();
                            Log.i("mytag", "place not found" + e.getMessage());
                            Log.i("mytag", "status code: " + StatusCode);
                        }
                    }
                });
            }

            @Override
            public void OnItemDeleteListener(int position, View v) {

            }
        });

        mInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("mytag", "onClick: clicked place info ");
                try {
                    if (mMarker.isInfoWindowShown()) {
                        Log.d("mytag", "onClick: isInfoWindowShown ");
                        mMarker.hideInfoWindow();
                    } else {
                        Log.d("mytag", "onClick: place info " + mPlace.toString());
                        mMarker.showInfoWindow();
                    }
                } catch (NullPointerException e) {
                    Log.e("mytag", "onClick : NullPointerException: " + e.getMessage());
                }
            }
        });

        mFavorite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PlaceName = place.getName();
                PlaceAddress = place.getAddress();

                Log.d("mytag", "onClick: Favorite Name: " + PlaceName + " Address: " + PlaceAddress);

                if(tourist) {
                    String id = userDatabase.push().getKey();
                    Favorites favorites = new Favorites(PlaceName,PlaceAddress,id);
                    userDatabase.child(id).setValue(favorites).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(MapActivity.this, "Place added to favorites", Toast.LENGTH_LONG).show();
                        }
                    });
                } else {
                    String id = guideDatabase.push().getKey();
                    Favorites favorites = new Favorites(PlaceName,PlaceAddress,id);
                    guideDatabase.child(id).setValue(favorites).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(MapActivity.this, "Place added to favorites", Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        });

        mAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("mytag", "onClick: Add ");
                getAllListsFromDB();
                add_placeDialog.show();
            }
        });

        if (savedInstanceState == null) {
            nav_view.setCheckedItem(R.id.map_menu);
        }

        if(tourist) {
            userDatabase.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    fav_haveChildren = dataSnapshot.hasChildren();
                    Log.d("mytag", "onDataChange: MapActivity userDatabase have kids " + fav_haveChildren);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                }
            });
        } else {
            guideDatabase.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    fav_haveChildren = dataSnapshot.hasChildren();
                    Log.d("mytag", "onDataChange: MapActivity guideDatabase have kids " + fav_haveChildren);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                }
            });
        }

        // ********************************************** ADD PLACE DIALOG **********************************************************

        add_placeDialog = new Dialog(MapActivity.this);
        add_placeDialog.setContentView(R.layout.add_place_dialog);
        add_placeDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        add_placeDialog.setCancelable(false);

        add_place_dialog_btn_Cancel = add_placeDialog.findViewById(R.id.add_place_dialog_btn_Cancel);
        add_place_dialog_btn_Ok = add_placeDialog.findViewById(R.id.add_place_dialog_btn_Ok);
        add_place_spinner = add_placeDialog.findViewById(R.id.add_place_dialog_spinner);

        add_place_dialog_btn_Ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ListChosen = add_place_spinner.getSelectedItem().toString();
                PlaceName = place.getName();
                Log.d("mytag", "onClick: MapActivity Chosen list from spinner " + ListChosen);
                Query query = myTourUserDatabase.orderByChild("tourTitle").equalTo(ListChosen);
                query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.exists()) {
                            for (DataSnapshot issue : snapshot.getChildren()) {
                                ListID = issue.getKey();
                                Log.d("mytag", "onClick: MapActivity Chosen list ref from spinner " + ListID);
                                TourListChosen_UserDB = FirebaseDatabase.getInstance().getReference("UsersToursList").child(userID).child(ListID).child("places");

                                PlaceInList place = new PlaceInList(PlaceName);
                                TourListChosen_UserDB.push().setValue(place);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
                add_placeDialog.dismiss();
            }
        });

        add_place_dialog_btn_Cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                add_placeDialog.dismiss();
            }
        });
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {
        HideKeyboard();
        mMap = googleMap;
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);

        // ********************************************** LOCATION BUTTON POSITIONING **********************************************************
        if (mapView != null && mapView.findViewById(Integer.parseInt("1")) != null) {
            View locationButton = ((View) mapView.findViewById(Integer.parseInt("1")).getParent()).findViewById(Integer.parseInt("2"));
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) locationButton.getLayoutParams();
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
            layoutParams.setMargins(0, 0, 40, 150);
        }

        // ********************************************** COMPASS BUTTON POSITIONING **********************************************************
        View compassButton = mapView.findViewWithTag("GoogleMapCompass");//to access the compass button
        RelativeLayout.LayoutParams rlp = (RelativeLayout.LayoutParams) compassButton.getLayoutParams();
        rlp.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
        rlp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
        rlp.setMargins(40, 0, 0, 150);


        //check if gps is enabled or not and then request user to enable it
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);

        SettingsClient settingsClient = LocationServices.getSettingsClient(MapActivity.this);
        Task<LocationSettingsResponse> task = settingsClient.checkLocationSettings(builder.build());

        task.addOnSuccessListener(MapActivity.this, new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                getDeviceLocation();
            }
        });

        task.addOnFailureListener(MapActivity.this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if (e instanceof ResolvableApiException) {
                    ResolvableApiException resolvable = (ResolvableApiException) e;
                    try {
                        resolvable.startResolutionForResult(MapActivity.this, 50);
                    } catch (IntentSender.SendIntentException sendIntentException) {
                        sendIntentException.printStackTrace();
                    }
                }
            }
        });

        mMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
            @Override
            public boolean onMyLocationButtonClick() {
                HideKeyboard();
                mInfo.setVisibility(View.GONE);
                mAdd.setVisibility(View.GONE);
                mFavorite.setVisibility(View.GONE);
                if (materialSearchBar.isSuggestionsVisible()) {
                    materialSearchBar.clearSuggestions();
                    mMap.clear();
                }
                if (materialSearchBar.isSearchEnabled()) {
                    materialSearchBar.clearFocus();
                    materialSearchBar.disableSearch();
                    mMap.clear();
                }
                return false;
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == 50) {
            if (resultCode == RESULT_OK) {
                getDeviceLocation();
            }
        }
    }

    @SuppressLint("MissingPermission")
    private void getDeviceLocation() {
        mfusedLocationProviderClient.getLastLocation()
                .addOnCompleteListener(new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful()) {
                            mLastKnownLocation = task.getResult();
                            if (mLastKnownLocation != null) {
                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude()), DEFAULT_ZOOM));
                            } else {
                                final LocationRequest locationRequest = LocationRequest.create();
                                locationRequest.setInterval(10000);
                                locationRequest.setFastestInterval(5000);
                                locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                                locationCallback = new LocationCallback() {
                                    @Override
                                    public void onLocationResult(LocationResult locationResult) {
                                        super.onLocationResult(locationResult);
                                        if (LocationRequest.create() == null) {
                                            return;
                                        }
                                        mLastKnownLocation = locationResult.getLastLocation();
                                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude()), DEFAULT_ZOOM));
                                        mfusedLocationProviderClient.removeLocationUpdates(locationCallback);
                                    }
                                };
                                mfusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null);
                            }
                        } else {
                            Toast.makeText(MapActivity.this, "Unable to get last location", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    @Override
    public void onBackPressed() {
        materialSearchBar.disableSearch();
        materialSearchBar.clearSuggestions();
        materialSearchBar.clearFocus();

        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce=false;
            }
        }, 2000);

        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            return;
        }
    }


    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.map_menu:
                nav_view.setCheckedItem(R.id.map_menu);
                break;
            case R.id.logout_menu:
                mAuth.signOut();
                startActivity(new Intent(getApplicationContext(), Login.class));
                finish();
                break;
            case R.id.my_profile_menu:
                accountTypeProfile(tourist);
                break;

            case R.id.favorites_menu:
                accountTypeFavorite(tourist);
                break;

            case R.id.my_tours_menu:
                accountTypeMyTours(tourist);
                break;
        }
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void PlaceInfo(LatLng latLng, float zoom, PlaceInfo placeInfo) {
        HideKeyboard();
        mMap.setInfoWindowAdapter(new CustomInfoWindowAdapter(MapActivity.this));

        if (placeInfo != null) {
            Log.d("mytag", "PlaceInfo: placeInfo != null");
            try {
                String snippet = "Address : " + placeInfo.getAddress() + "\n" +
                        "Phone Number : " + placeInfo.getPhoneNumber() + "\n" +
                        "Website : " + placeInfo.getWebsiteUri() + "\n" +
                        "Rating : " + placeInfo.getRating() + "\n" +
                        "User Total Rating : " + placeInfo.getUserRatingsTotal();
                mInfo.setVisibility(View.VISIBLE);
                mFavorite.setVisibility(View.VISIBLE);
                if(tourist)
                    mAdd.setVisibility(View.VISIBLE);
                else
                    mAdd.setVisibility(View.GONE);

                MarkerOptions options = new MarkerOptions()
                    .position(latLng)
                    .title(placeInfo.getName())
                    .snippet(snippet);
            mMarker = mMap.addMarker(options);

        } catch(NullPointerException e){
            Log.e("mytag", "PlaceInfo: NullPointerException: " + e.getMessage());
        }
    }else

    {
        mMap.addMarker(new MarkerOptions().position(latLng));
    }
}

    public void accountTypeProfile(boolean tourist) {
        Intent intent = new Intent(MapActivity.this, ProfileActivity.class);
        intent.putExtra("showFav",fav_haveChildren);
        if (tourist) {
            intent.putExtra("tourist", true);
        } else {
            intent.putExtra("tourist", false);
        }
        startActivity(intent);
        finish();
    }

    public void accountTypeFavorite(boolean tourist) {
        Intent intent = new Intent(MapActivity.this, FavoriteActivity.class);
        intent.putExtra("showFav",fav_haveChildren);
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
            intent = new Intent(MapActivity.this, MyToursActivity.class);
            intent.putExtra("showFav",fav_haveChildren);
            intent.putExtra("tourist", true);
        } else {
            intent = new Intent(MapActivity.this, GuideMyTours.class);
            intent.putExtra("showFav",fav_haveChildren);
            intent.putExtra("tourist", false);
        }
        startActivity(intent);
        finish();
    }

    public void getAllListsFromDB() {
        listener = myTourUserDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                arrayList.clear();
                for (DataSnapshot tourList : snapshot.getChildren()) {
                    arrayList.add(tourList.child("tourTitle").getValue(String.class));
                }
                ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(MapActivity.this,R.layout.style_spinner,arrayList);
                add_place_spinner.setAdapter(arrayAdapter);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private void HideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(drawer.getWindowToken(), 0);
    }
}