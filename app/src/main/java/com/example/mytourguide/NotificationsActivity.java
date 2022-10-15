package com.example.mytourguide;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.util.Calendar;

import de.hdodenhof.circleimageview.CircleImageView;

public class NotificationsActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawer;
    private NavigationView nav_view;

    TextView nav_header_tvDate;
    CircleImageView nav_header_img;

    ConstraintLayout mainLayout;

    FirebaseAuth fAuth;
    StorageReference storageReference;

    private static boolean tourist;
    private static boolean fav_haveChildren;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);

        Toolbar toolbar = findViewById(R.id.notification_toolbar);
        setSupportActionBar(toolbar);

        drawer = findViewById(R.id.notification_drawer_layout);
        nav_view = findViewById(R.id.notification_nav_view);

        nav_view.bringToFront();
        nav_view.setNavigationItemSelectedListener(this);

        mainLayout = findViewById(R.id.notification_mainLayout);


        fAuth = FirebaseAuth.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();


        View header = nav_view.getHeaderView(0);
        nav_header_img = header.findViewById(R.id.nav_header_img);

        nav_header_tvDate = header.findViewById(R.id.nav_header_tv_date);
        Calendar calendar = Calendar.getInstance();
        String currentDate = DateFormat.getDateInstance(DateFormat.FULL).format(calendar.getTime());
        nav_header_tvDate.setText(currentDate);

        tourist = getIntent().getExtras().getBoolean("tourist");
        fav_haveChildren = getIntent().getExtras().getBoolean("showFav");

        if(tourist) {
            nav_header_img.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_tourist));
            Log.d("TAG2", "NotificationsActivity - onCreate: " + tourist);
            StorageReference profileRef = storageReference.child("users/"+fAuth.getCurrentUser().getUid()+"/profile.jpg");
            profileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    Picasso.get().load(uri).into(nav_header_img);
                }
            });
        } else {
            nav_header_img.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_tour_guide));
            Log.d("TAG2", "NotificationsActivity - onCreate: " + tourist);
            StorageReference profileRef = storageReference.child("guides/"+fAuth.getCurrentUser().getUid()+"/profile.jpg");
            profileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    Picasso.get().load(uri).into(nav_header_img);
                }
            });
        }

        ActionBarDrawerToggle fav_toggle = new ActionBarDrawerToggle(this, drawer, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(fav_toggle);
        fav_toggle.syncState();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.map_menu:
                accountTypeMap(tourist);
                break;

            case R.id.logout_menu:
                fAuth.signOut();
                startActivity(new Intent(getApplicationContext(),Login.class));
                finish();
                break;

            case R.id.my_profile_menu:
                accountTypeProfile(tourist);
                break;

            case R.id.favorites_menu:
                accountTypeFavorites(tourist);
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
        Intent intent = new Intent(NotificationsActivity.this, MapActivity.class);
        intent.putExtra("showFav",fav_haveChildren);
        if (tourist) {
            intent.putExtra("tourist", true);
        } else {
            intent.putExtra("tourist", false);
        }
        startActivity(intent);
        finish();
    }

    public void accountTypeProfile(boolean tourist) {
        Intent intent = new Intent(NotificationsActivity.this, ProfileActivity.class);
        intent.putExtra("showFav",fav_haveChildren);
        if (tourist) {
            intent.putExtra("tourist", true);
        } else {
            intent.putExtra("tourist", false);
        }
        startActivity(intent);
        finish();
    }

    public void accountTypeFavorites(boolean tourist) {
        Intent intent = new Intent(NotificationsActivity.this, FavoriteActivity.class);
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
            intent = new Intent(NotificationsActivity.this, MyToursActivity.class);
            intent.putExtra("showFav",fav_haveChildren);
            intent.putExtra("tourist", true);
        } else {
            intent = new Intent(NotificationsActivity.this, GuideMyTours.class);
            intent.putExtra("showFav",fav_haveChildren);
            intent.putExtra("tourist", false);
        }
        startActivity(intent);
        finish();
    }
}