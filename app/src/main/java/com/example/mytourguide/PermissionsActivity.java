package com.example.mytourguide;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

public class PermissionsActivity extends AppCompatActivity {

    private Button btn_grant;
    ImageView img;
    TextView tv_title, tv_infoText;
    private static final String TAG1 = "PermissionsActivity";
    private static boolean tourist;
    private static boolean Allowed;
    FirebaseAuth mAuth;
    FirebaseFirestore fStore;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_permissions);

        btn_grant = findViewById(R.id.permission_btn_grant);
        img = findViewById(R.id.permission_img);
        tv_title = findViewById(R.id.permission_tv_title);
        tv_infoText = findViewById(R.id.permission_tv_infoText);

        mAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
        Granted(Allowed);
        Log.d("TAG1", "PermissionsActivity - onCreate: Granted " + Allowed);

        tourist = getIntent().getExtras().getBoolean("tourist");
        Log.d("TAG1", "PermissionsActivity - onCreate: Intent " + tourist);

        String ID = mAuth.getCurrentUser().getUid();
        DocumentReference docIdRef = fStore.collection("users").document(ID);

        docIdRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        if (document.get("username") != null) {
                            tourist = true;
                            accountType(tourist);
                            Log.d("TAG1", "PermissionsActivity - onComplete: field exists " + "tourist = true");
                        }
                    } else {
                        tourist = false;
                        accountType(tourist);
                        Log.d("TAG1", "PermissionsActivity - onComplete: field does not exist " + "tourist = false");
                    }
                }
            }
        });

        btn_grant.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Dexter.withActivity(PermissionsActivity.this)
                        .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                        .withListener(new PermissionListener() {
                            @Override
                            public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {
                                Intent intent = new Intent(PermissionsActivity.this, MapActivity.class);
                                if (tourist) {
                                    Granted(true);
                                    intent.putExtra("tourist", true);
                                    Log.d("TAG1", "PermissionsActivity - onPermissionGranted: " + tourist);
                                } else {
                                    intent.putExtra("tourist", false);
                                    Log.d("TAG1", "PermissionsActivity - onPermissionGranted: " + tourist);
                                }
                                startActivity(intent);
                                finish();
                            }

                            @Override
                            public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse) {
                                if (permissionDeniedResponse.isPermanentlyDenied()) {
                                    AlertDialog.Builder builder = new AlertDialog.Builder(PermissionsActivity.this);
                                    builder.setTitle("Permission Denied")
                                            .setMessage("Permission to access device location is permanently denied. you need to go to settings to allow the permission.")
                                            .setNegativeButton("Cancel", null)
                                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    Intent intent = new Intent();
                                                    intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                                    intent.setData(Uri.fromParts("package", getPackageName(), null));
                                                }
                                            })
                                            .show();
                                } else {
                                    Toast.makeText(PermissionsActivity.this, "Permission Denied", Toast.LENGTH_LONG).show();
                                }
                            }

                            @Override
                            public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken) {
                                permissionToken.continuePermissionRequest();
                            }
                        })
                        .check();
            }
        });
    }

    public void accountType(boolean tourist) {
        if (ContextCompat.checkSelfPermission(PermissionsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Intent intent = new Intent(PermissionsActivity.this, MapActivity.class);
            if (tourist) {
                intent.putExtra("tourist", true);
            } else {
                intent.putExtra("tourist", false);
            }
            startActivity(intent);
            finish();
        }
    }

    public void Granted(boolean Allowed) {
        if (ContextCompat.checkSelfPermission(PermissionsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Allowed = true;
            img.setVisibility(View.GONE);
            tv_title.setVisibility(View.GONE);
            tv_infoText.setVisibility(View.GONE);
            btn_grant.setVisibility(View.GONE);
            Log.d("TAG1", "Granted: Blank Activity!");
        } else {
            Allowed = false;
            img.setVisibility(View.VISIBLE);
            tv_title.setVisibility(View.VISIBLE);
            tv_infoText.setVisibility(View.VISIBLE);
            btn_grant.setVisibility(View.VISIBLE);
            Log.d("TAG1", "Granted: Normal Activity");

        }
    }
}