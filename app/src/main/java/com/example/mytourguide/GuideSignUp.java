package com.example.mytourguide;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class GuideSignUp extends AppCompatActivity {

    ConstraintLayout mainLayout;
    EditText et_dob,et_phone;
    Button btn_upload, btn_signup,already_exist_dialog_btn_yes,
            already_exist_dialog_btn_no,guide_reg_dialog_btn_ok;
    ImageView card_img;
    CardView card_view_card_img;
    private DatePickerDialog.OnDateSetListener mDateSetListener;
    ProgressBar progress;
    StorageReference storageReference;
    FirebaseFirestore fStore;
    FirebaseAuth mAuth;
    FirebaseUser FB_user;
    Dialog already_existDialog,guide_regDialog;
    private static String UserName,FullName,E_Mail,Password,Phone,DOB,guideID;
    private static String enabled;

    private static final String TAG = "GuideSignUp";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guide_sign_up);

        mainLayout = findViewById(R.id.guide_reg_mainLayout);

        et_dob = findViewById(R.id.guide_reg_et_dob);
        et_phone = findViewById(R.id.guide_reg_et_phone);
        btn_upload = findViewById(R.id.guide_reg_btn_upload);
        btn_signup = findViewById(R.id.guide_reg_btn_signup);
        progress = findViewById(R.id.guide_reg_progress);
        card_img = findViewById(R.id.guide_reg_card_img);
        card_view_card_img = findViewById(R.id.guide_reg_card_view_card_img);

        mAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();
        FB_user = mAuth.getCurrentUser();

        et_dob.setInputType(InputType.TYPE_NULL);

        Intent data = getIntent();

        UserName = data.getStringExtra("username");
        FullName = data.getStringExtra("fName");
        E_Mail = data.getStringExtra("email");
        Password = data.getStringExtra("password");
        Phone = data.getStringExtra("phone");
        Log.d(TAG, "onCreate: Intent" + UserName + " / "+ FullName + " / "+
                E_Mail + " / "+ Password + " / "+ Phone);

        et_dob.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar cal = Calendar.getInstance();
                int year = cal.get(Calendar.YEAR);
                int month = cal.get(Calendar.MONTH);
                int day = cal.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog dialog = new DatePickerDialog(
                        GuideSignUp.this,
                        mDateSetListener,
                        year-25, month, day);
                dialog.show();
                dialog.setCancelable(false);
            }
        });

        mDateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int day) {
                month = month + 1;
                Log.d(TAG, "onDateSet: dd/mm/yyyy: " + day + "/" + month + "/" + year);

                String date = day + "/" + month + "/" + year;
                et_dob.setText(date);
            }
        };

        btn_upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent openGalleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(openGalleryIntent,1000);
            }
        });

        btn_signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(GuideSignUp.this, "You MUST upload a card pic", Toast.LENGTH_LONG).show();
            }
        });

        // ********************************************** ALREADY EXIST DIALOG **********************************************************
        already_existDialog = new Dialog(GuideSignUp.this);
        already_existDialog.setContentView(R.layout.already_exist_dialog);
        already_existDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        already_existDialog.setCancelable(false);

        already_exist_dialog_btn_yes = already_existDialog.findViewById(R.id.exist_dialog_btn_yes);
        already_exist_dialog_btn_no = already_existDialog.findViewById(R.id.exist_dialog_btn_no);

        already_exist_dialog_btn_yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), Login.class);
                startActivity(intent);
                finish();
            }
        });
        already_exist_dialog_btn_no.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                already_existDialog.dismiss();
            }
        });

        // ********************************************** GUIDE REGISTER DIALOG **********************************************************

        guide_regDialog = new Dialog(GuideSignUp.this);
        guide_regDialog.setContentView(R.layout.guide_reg_dialog);
        guide_regDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        guide_regDialog.setCancelable(false);

        guide_reg_dialog_btn_ok = guide_regDialog.findViewById(R.id.guide_reg_dialog_btn_ok);

        guide_reg_dialog_btn_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAuth.signOut();
                startActivity(new Intent(getApplicationContext(),Login.class));
                finish();
                guide_regDialog.dismiss();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @androidx.annotation.Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1000) {
            if (resultCode == Activity.RESULT_OK) {
                Uri imageUri = data.getData();
                card_img.setVisibility(View.VISIBLE);
                card_view_card_img.setVisibility(View.VISIBLE);
                card_img.setImageURI(imageUri);

                btn_signup.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        DOB = String.valueOf(et_dob.getText());
                        Phone = String.valueOf(et_phone.getText());
                        enabled = "false";
                        if (!Phone.equals("") && !DOB.equals("")) {
                            progress.setVisibility(View.VISIBLE);
                            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                            imm.hideSoftInputFromWindow(mainLayout.getWindowToken(), 0);
                            // SIGNING UP
                            Guide db_guide = new Guide(UserName, FullName, E_Mail, Password,Phone,DOB,enabled);
                            Log.d(TAG, "onActivityResult: Intent" + UserName + " / "+ FullName + " / "+
                                    E_Mail + " / "+ Password + " / "+ Phone);
                            mAuth.createUserWithEmailAndPassword(E_Mail, Password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        progress.setVisibility(View.VISIBLE);
                                        FB_user = mAuth.getCurrentUser();
                                        FB_user.sendEmailVerification().addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                guideID = mAuth.getCurrentUser().getUid();
                                                FirebaseUser currentUser = mAuth.getCurrentUser();
                                                UserProfileChangeRequest profileChangeRequest = new UserProfileChangeRequest.Builder().setDisplayName(FullName).build();
                                                currentUser.updateProfile(profileChangeRequest);
                                                DocumentReference documentReference = fStore.collection("guides").document(guideID);
                                                Map<String, Object> guide = new HashMap<>();
                                                guide.put("username", UserName);
                                                guide.put("fName", FullName);
                                                guide.put("email", E_Mail);
                                                guide.put("password", Password);
                                                guide.put("phone", Phone);
                                                guide.put("DOB", DOB);
                                                guide.put("enabled", enabled);
                                                guide.put("guideID",guideID);
                                                documentReference.set(guide);
                                                FirebaseDatabase.getInstance().getReference("Guides").child(FirebaseAuth.getInstance().
                                                        getCurrentUser().getUid()).setValue(guide);
                                                uploadImageToFireBase(imageUri);
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Log.d("TAG", "onFailure: Email not sent" + e.getMessage());
                                                progress.setVisibility(View.GONE);
                                            }
                                        });
                                    } else {
                                        if (task.getException().getMessage() == "The email address is already in use by another account.") {
                                            already_existDialog.show();
                                            progress.setVisibility(View.GONE);
                                        }
                                    }
                                }
                            });
                        } else {
                            Toast.makeText(getApplicationContext(), "All fields are required", Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        }
    }

    private void uploadImageToFireBase(Uri imageUri) {
        StorageReference FileReference = storageReference.child("guides/"+mAuth.getCurrentUser().getUid()+"/tour-guide-card.jpg");
        FileReference.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                FileReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Picasso.get().load(uri).into(card_img);
                    }
                });
                progress.setVisibility(View.GONE);
                guide_regDialog.show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(GuideSignUp.this, "Failed to upload.", Toast.LENGTH_LONG).show();
                progress.setVisibility(View.GONE);
            }
        });

    }

    @Override
    public void onBackPressed() {
        Intent i = new Intent(getApplicationContext(), Signup.class);
        i.putExtra("username",UserName);
        i.putExtra("fName",FullName);
        i.putExtra("email",E_Mail);
        i.putExtra("password",Password);
        startActivity(i);
        finish();
    }

}
