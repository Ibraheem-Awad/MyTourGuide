package com.example.mytourguide;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class EditProfile extends AppCompatActivity {

    Button change_profile_image,btn_cancel,btn_save,changes_saved_dialog_btn_ok;
    CircleImageView profile_img;
    EditText et_full_name,et_e_mail,et_phone;
    ProgressBar pic_progress;

    StorageReference storageReference;
    FirebaseAuth fAuth;
    FirebaseFirestore fStore;
    FirebaseUser user;
    Dialog changes_saved_Dialog;
    private static boolean tourist;
    private static boolean fav_haveChildren;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        change_profile_image = findViewById(R.id.edit_profile_btn_change_profile_image);
        profile_img = findViewById(R.id.edit_profile_img);
        btn_cancel = findViewById(R.id.edit_profile_btn_cancel);
        btn_save = findViewById(R.id.edit_profile_btn_save);
        et_full_name = findViewById(R.id.edit_profile_et_full_name);
        et_e_mail = findViewById(R.id.edit_profile_et_e_mail);
        et_phone = findViewById(R.id.edit_profile_et_phone);
        pic_progress = findViewById(R.id.change_profile_pic_progress);

        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();
        user = fAuth.getCurrentUser();

        Intent data = getIntent();
        String FullName = data.getStringExtra("fName");
        String E_Mail = data.getStringExtra("email");
        String Phone = data.getStringExtra("phone");

        tourist = getIntent().getExtras().getBoolean("tourist");
        fav_haveChildren = getIntent().getExtras().getBoolean("showFav");


        et_full_name.setText(FullName);
        et_e_mail.setText(E_Mail);
        et_phone.setText(Phone);

        change_profile_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //open gallery
                Intent openGalleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(openGalleryIntent,1000);
            }
        });

        if(tourist) {
            StorageReference profileRef = storageReference.child("users/"+fAuth.getCurrentUser().getUid()+"/profile.jpg");
            profileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    Picasso.get().load(uri).into(profile_img);
                }
            });
        } else {
            profile_img.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_tour_guide));
            StorageReference profileRef = storageReference.child("guides/"+fAuth.getCurrentUser().getUid()+"/profile.jpg");
            profileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    Picasso.get().load(uri).into(profile_img);
                }
            });
        }

        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), ProfileActivity.class);
                intent.putExtra("showFav",fav_haveChildren);
                if (tourist) {
                    intent.putExtra("tourist", true);
                } else {
                    intent.putExtra("tourist", false);
                }
                startActivity(intent);
                finish();
            }
        });

        //***************************************** WITHOUT CHANGING PROFILE PIC *********************************************************
        btn_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(et_full_name.getText().toString().isEmpty()
                        || et_e_mail.getText().toString().isEmpty()
                        || et_phone.getText().toString().isEmpty()) {
                    Toast.makeText(EditProfile.this, "You can't leave one of these fields empty", Toast.LENGTH_LONG).show();
                    return;
                }
                pic_progress.setVisibility(View.VISIBLE);
                String FullName = et_full_name.getText().toString();
                String Email = et_e_mail.getText().toString();
                String Phone = et_phone.getText().toString();
                user.updateEmail(Email).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        if(tourist) {
                            DocumentReference docRef = fStore.collection("users").document(user.getUid());
                            Map<String, Object> edited = new HashMap<>();
                            edited.put("fName", FullName);
                            edited.put("email", Email);
                            edited.put("phone", Phone);
                            FirebaseDatabase.getInstance().getReference("Users").child(FirebaseAuth.getInstance().
                                    getCurrentUser().getUid()).updateChildren(edited);
                            docRef.update(edited).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    pic_progress.setVisibility(View.GONE);
                                    changes_saved_Dialog.show();
                                }
                            });
                        } else {
                            DocumentReference docRef = fStore.collection("guides").document(user.getUid());
                            Map<String, Object> edited = new HashMap<>();
                            edited.put("fName", FullName);
                            edited.put("email", Email);
                            edited.put("phone", Phone);
                            FirebaseDatabase.getInstance().getReference("Guides").child(FirebaseAuth.getInstance().
                                    getCurrentUser().getUid()).updateChildren(edited);
                            docRef.update(edited).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    pic_progress.setVisibility(View.GONE);
                                    changes_saved_Dialog.show();
                                }
                            });
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(EditProfile.this, e.getMessage(), Toast.LENGTH_LONG).show();
                        pic_progress.setVisibility(View.GONE);
                    }
                });
            }
        });

        // ********************************************** CHANGES SAVED DIALOG **********************************************************

        changes_saved_Dialog = new Dialog(EditProfile.this);
        changes_saved_Dialog.setContentView(R.layout.changes_saved_dialog);
        changes_saved_Dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        changes_saved_Dialog.setCancelable(false);

        changes_saved_dialog_btn_ok = changes_saved_Dialog.findViewById(R.id.changes_saved_dialog_btn_ok);

        changes_saved_dialog_btn_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), ProfileActivity.class);
                intent.putExtra("showFav",fav_haveChildren);
                if (tourist) {
                    intent.putExtra("tourist", true);
                } else {
                    intent.putExtra("tourist", false);
                }
                startActivity(intent);
                finish();
                changes_saved_Dialog.dismiss();
            }
        });
    }

    //***************************************** WITH CHANGING PROFILE PIC *********************************************************
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @androidx.annotation.Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 1000) {
            if(resultCode == Activity.RESULT_OK) {
                Uri imageUri = data.getData();
                profile_img.setImageURI(imageUri);

                btn_save.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(et_full_name.getText().toString().isEmpty()
                                || et_e_mail.getText().toString().isEmpty()
                                || et_phone.getText().toString().isEmpty()) {
                            Toast.makeText(EditProfile.this, "You can't leave one of these fields empty", Toast.LENGTH_LONG).show();
                            return;
                        }
                        pic_progress.setVisibility(View.VISIBLE);
                        String FullName = et_full_name.getText().toString();
                        String Email = et_e_mail.getText().toString();
                        String Phone = et_phone.getText().toString();
                        user.updateEmail(Email).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                if(tourist) {
                                    DocumentReference docRef = fStore.collection("users").document(user.getUid());
                                    Map<String, Object> edited = new HashMap<>();
                                    edited.put("fName", FullName);
                                    edited.put("email", Email);
                                    edited.put("phone", Phone);
                                    docRef.update(edited);
                                    FirebaseDatabase.getInstance().getReference("Users").child(FirebaseAuth.getInstance().
                                            getCurrentUser().getUid()).updateChildren(edited);
                                    uploadImageToFireBaseTourist(imageUri);
                                } else {
                                    DocumentReference docRef = fStore.collection("guides").document(user.getUid());
                                    Map<String, Object> edited = new HashMap<>();
                                    edited.put("fName", FullName);
                                    edited.put("email", Email);
                                    edited.put("phone", Phone);
                                    docRef.update(edited);
                                    FirebaseDatabase.getInstance().getReference("Guides").child(FirebaseAuth.getInstance().
                                            getCurrentUser().getUid()).updateChildren(edited);
                                    uploadImageToFireBaseGuide(imageUri);
                                }
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(EditProfile.this, e.getMessage(), Toast.LENGTH_LONG).show();
                                pic_progress.setVisibility(View.GONE);
                            }
                        });
                    }
                });
            }
        }
    }

    private void uploadImageToFireBaseTourist(Uri imageUri) {
        StorageReference FileReference = storageReference.child("users/"+fAuth.getCurrentUser().getUid()+"/profile.jpg");
        FileReference.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                FileReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Picasso.get().load(uri).into(profile_img);
                    }
                });
                pic_progress.setVisibility(View.GONE);
                changes_saved_Dialog.show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(EditProfile.this, "Failed to upload profile picture", Toast.LENGTH_LONG).show();
                pic_progress.setVisibility(View.GONE);
                Intent intent = new Intent(getApplicationContext(), ProfileActivity.class);
                intent.putExtra("showFav",fav_haveChildren);
                if (tourist)
                    intent.putExtra("tourist", true);
                startActivity(intent);
                finish();
            }
        });
    }

    private void uploadImageToFireBaseGuide(Uri imageUri) {
        StorageReference FileReference = storageReference.child("guides/"+fAuth.getCurrentUser().getUid()+"/profile.jpg");
        FileReference.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                FileReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Picasso.get().load(uri).into(profile_img);
                    }
                });
                pic_progress.setVisibility(View.GONE);
                changes_saved_Dialog.show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(EditProfile.this, "Failed to upload profile picture", Toast.LENGTH_LONG).show();
                pic_progress.setVisibility(View.GONE);
                Intent intent = new Intent(getApplicationContext(), ProfileActivity.class);
                intent.putExtra("showFav",fav_haveChildren);
                if (!tourist)
                    intent.putExtra("tourist", false);
                startActivity(intent);
                finish();
            }
        });
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(getApplicationContext(), ProfileActivity.class);
        intent.putExtra("showFav",fav_haveChildren);
        if (tourist) {
            intent.putExtra("tourist", true);
        } else {
            intent.putExtra("tourist", false);
        }
        startActivity(intent);
        finish();
    }
}