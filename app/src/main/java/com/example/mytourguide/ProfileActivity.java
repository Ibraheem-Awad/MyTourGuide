package com.example.mytourguide;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
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
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.util.Calendar;

import javax.annotation.Nullable;

import de.hdodenhof.circleimageview.CircleImageView;


public class ProfileActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private DrawerLayout drawer;
    private NavigationView nav_view;

    TextView user_name_user,user_full_name,user_email,user_phone,verifyMsg,nav_header_tvDate;
    Button edit_profile,rest_pass,logout,verify_email,
            verification_dialog_btn_resend,verification_dialog_btn_ok,
            rest_dialog_btn_Cancel,rest_dialog_btn_Ok;
    CircleImageView profile_img,nav_header_img;
    FirebaseUser user;
    String userId;
    FirebaseAuth fAuth;
    FirebaseFirestore fStore;
    StorageReference storageReference;

    Dialog resend_verification_Dialog,rest_passDialog;
    TextInputEditText rest_dialog_tie_Email;
    ConstraintLayout mainLayout;
    private static boolean tourist;
    private static boolean fav_haveChildren;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        Toolbar toolbar = findViewById(R.id.profile_toolbar);
        setSupportActionBar(toolbar);

        drawer = findViewById(R.id.profile_drawer_layout);
        nav_view = findViewById(R.id.profile_nav_view);
        nav_view.bringToFront();
        nav_view.setNavigationItemSelectedListener(this);

        mainLayout = findViewById(R.id.profile_mainLayout);
        user_name_user = findViewById(R.id.tv_profile_user_name_user);
        user_full_name = findViewById(R.id.tv_profile_user_full_name);
        user_email = findViewById(R.id.tv_profile_user_email);
        user_phone = findViewById(R.id.tv_profile_user_phone);
        verifyMsg = findViewById(R.id.tv_profile_verifyMsg);
        profile_img = findViewById(R.id.profile_img);
        edit_profile = findViewById(R.id.btn_profile_edit_profile);
        rest_pass = findViewById(R.id.btn_profile_rest_pass);
        logout = findViewById(R.id.btn_profile_logout);
        verify_email = findViewById(R.id.btn_profile_verify_email);

        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();

        userId = fAuth.getCurrentUser().getUid();
        user = fAuth.getCurrentUser();

        user.reload();
        boolean reloaded = true;
        nav_view.setCheckedItem(R.id.my_profile_menu);

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
            Log.d("TAG1", "ProfileActivity - onCreate: " + tourist);
            StorageReference profileRef = storageReference.child("users/"+fAuth.getCurrentUser().getUid()+"/profile.jpg");
            profileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    Picasso.get().load(uri).into(nav_header_img);
                    Picasso.get().load(uri).into(profile_img);
                }
            });
        } else {
            nav_header_img.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_tour_guide));
            profile_img.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_tour_guide));
            Log.d("TAG1", "ProfileActivity - onCreate: " + tourist);
            StorageReference profileRef = storageReference.child("guides/"+fAuth.getCurrentUser().getUid()+"/profile.jpg");
            profileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    Picasso.get().load(uri).into(nav_header_img);
                    Picasso.get().load(uri).into(profile_img);
                }
            });
        }

        if(reloaded) {
            if (!user.isEmailVerified()) {
                verifyMsg.setVisibility(View.VISIBLE);
                verify_email.setVisibility(View.VISIBLE);
                logout.setVisibility(View.GONE);
                verify_email.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(final View v) {
                        resend_verification_Dialog.show();
                    }
                });
            }
        }

        rest_pass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rest_passDialog.show();
            }
        });

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fAuth.signOut();
                startActivity(new Intent(getApplicationContext(),Login.class));
                finish();
            }
        });

        if(tourist) {
            DocumentReference documentReference = fStore.collection("users").document(userId);
            documentReference.addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
                @Override
                public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                    if(documentSnapshot.exists()){
                        user_name_user.setText(documentSnapshot.getString("username"));
                        user_full_name.setText(documentSnapshot.getString("fName"));
                        user_email.setText(documentSnapshot.getString("email"));
                        user_phone.setText(documentSnapshot.getString("phone"));

                    }else {
                        Log.d("tag", "onEvent: Document do not exists");
                    }
                }
            });
        }
        else {
            DocumentReference guide_documentReference = fStore.collection("guides").document(userId);
            guide_documentReference.addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
                @Override
                public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                    if(documentSnapshot.exists()){
                        user_name_user.setText(documentSnapshot.getString("username"));
                        user_full_name.setText(documentSnapshot.getString("fName"));
                        user_email.setText(documentSnapshot.getString("email"));
                        user_phone.setText(documentSnapshot.getString("phone"));

                    }else {
                        Log.d("tag", "onEvent: Document do not exists");
                    }
                }
            });
        }

        edit_profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                accountTypeEditProfile(tourist);
            }
        });

        // ********************************************** RESEND VERIFICATION DIALOG ****************************************************

        resend_verification_Dialog = new Dialog(ProfileActivity.this);
        resend_verification_Dialog.setContentView(R.layout.resend_verification_dialog);
        resend_verification_Dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        resend_verification_Dialog.setCancelable(false);

        verification_dialog_btn_resend = resend_verification_Dialog.findViewById(R.id.verification_dialog_btn_resend);
        verification_dialog_btn_ok = resend_verification_Dialog.findViewById(R.id.verification_dialog_btn_Ok);

        verification_dialog_btn_resend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                user = fAuth.getCurrentUser();
                user.sendEmailVerification().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(ProfileActivity.this, "Verification Email has been sent", Toast.LENGTH_LONG).show();
                        resend_verification_Dialog.dismiss();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("TAG", "onFailure: Email not sent" + e.getMessage());
                        resend_verification_Dialog.dismiss();
                    }
                });
            }
        });

        verification_dialog_btn_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resend_verification_Dialog.dismiss();
            }
        });

        // ********************************************** REST PASSWORD DIALOG **********************************************************

        rest_passDialog = new Dialog(ProfileActivity.this);
        rest_passDialog.setContentView(R.layout.forgotpass_dialog);
        rest_passDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        rest_passDialog.setCancelable(false);

        rest_dialog_btn_Cancel = rest_passDialog.findViewById(R.id.rest_dialog_btn_Cancel);
        rest_dialog_btn_Ok = rest_passDialog.findViewById(R.id.rest_dialog_btn_Ok);
        rest_dialog_tie_Email = rest_passDialog.findViewById(R.id.rest_dialog_tie_Email);

        rest_dialog_btn_Ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String rest_email = rest_dialog_tie_Email.getText().toString();
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(mainLayout.getWindowToken(), 0);
                if (!rest_email.equals("")) {
                    fAuth.sendPasswordResetEmail(rest_email).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(ProfileActivity.this, "Rest link sent to your Email, please check your inbox.", Toast.LENGTH_LONG).show();
                            rest_passDialog.dismiss();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(ProfileActivity.this, "Error! Rest link is not sent, " + e.getMessage(), Toast.LENGTH_LONG).show();
                            rest_passDialog.dismiss();
                        }
                    });
                } else {
                    Toast.makeText(getApplicationContext(), "You should enter you Email address, rest link is not sent", Toast.LENGTH_LONG).show();
                }
            }
        });

        rest_dialog_btn_Cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rest_passDialog.dismiss();
            }
        });

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
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
                nav_view.setCheckedItem(R.id.my_profile_menu);
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

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            accountTypeMap(tourist);
        }
    }

    public void accountTypeEditProfile(boolean tourist) {
        Intent intent = new Intent(ProfileActivity.this, EditProfile.class);
        intent.putExtra("fName",user_full_name.getText().toString());
        intent.putExtra("email",user_email.getText().toString());
        intent.putExtra("phone",user_phone.getText().toString());
        intent.putExtra("showFav",fav_haveChildren);
        if (tourist) {
            intent.putExtra("tourist", true);
        } else {
            intent.putExtra("tourist", false);
        }
        startActivity(intent);
        finish();
    }

    public void accountTypeMap(boolean tourist) {
        Intent intent = new Intent(ProfileActivity.this, MapActivity.class);
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
        Intent intent = new Intent(ProfileActivity.this, FavoriteActivity.class);
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
            intent = new Intent(ProfileActivity.this, MyToursActivity.class);
            intent.putExtra("showFav",fav_haveChildren);
            intent.putExtra("tourist", true);
        } else {
            intent = new Intent(ProfileActivity.this, GuideMyTours.class);
            intent.putExtra("showFav",fav_haveChildren);
            intent.putExtra("tourist", false);
        }
        startActivity(intent);
        finish();
    }
}
