package com.example.mytourguide;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class Signup extends AppCompatActivity {
    private static final String TAG = "Signup";
    TextInputEditText tie_fullname, tie_email, tie_username, tie_password;
    Button btn_Signup,confirm_email_dialog_btn_Ok,already_exist_dialog_btn_yes,already_exist_dialog_btn_no;
    TextView tv_signup, tv_loginText,tv_rule1,tv_rule2,tv_rule3,tv_rule4;
    ProgressBar progressBar;
    private CardView frameOne, frameTwo, frameThree, frameFour;
    private boolean isAtLeast8 = false, hasUppercase = false, hasNumber = false, hasSymbol = false;
    FirebaseAuth mAuth;
    FirebaseUser FB_user;
    FirebaseFirestore fStore;
    Dialog confirm_emailDialog,already_existDialog;
    ConstraintLayout reg_mainLayout;
    String userID;
    private static String fullname, username, password, email, phone;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        reg_mainLayout = findViewById(R.id.reg_mainLayout);
        tie_fullname = findViewById(R.id.reg_tie_fullname);
        tie_email = findViewById(R.id.reg_tie_email);
        tie_username = findViewById(R.id.reg_tie_username);
        tie_password = findViewById(R.id.reg_tie_password);
        btn_Signup = findViewById(R.id.reg_btn_Signup);
        tv_signup = findViewById(R.id.reg_tv_signup);
        tv_loginText = findViewById(R.id.reg_tv_loginText);
        progressBar = findViewById(R.id.reg_progress);
        frameOne = findViewById(R.id.frameOne_rule1);
        frameTwo = findViewById(R.id.frameTwo_rule2);
        frameThree = findViewById(R.id.frameThree_rule3);
        frameFour = findViewById(R.id.frameFour_rule4);
        tv_rule1 = findViewById(R.id.reg_tv_rule1);
        tv_rule2 = findViewById(R.id.reg_tv_rule2);
        tv_rule3 = findViewById(R.id.reg_tv_rule3);
        tv_rule4 = findViewById(R.id.reg_tv_rule4);
        mAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();

        Intent data = getIntent();
        username = data.getStringExtra("username");
        fullname = data.getStringExtra("fName");
        email = data.getStringExtra("email");
        password = data.getStringExtra("password");

        if(username != null && fullname != null && email != null && password != null) {
            tie_username.setText(username);
            tie_fullname.setText(fullname);
            tie_email.setText(email);
            tie_password.setText(password);
            PasswordCheck_UI();
        }
        Log.d(TAG, "onCreate: Intent " + username + " / "+ fullname + " / "+
                email + " / "+ password + " / "+ phone);

        boolean tourist = getIntent().getExtras().getBoolean("tourist");

        if(!tourist) {
            btn_Signup.setText("NEXT");
            Log.d(TAG, "onCreate: tourist " + tourist);
        }

        tv_loginText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), Login.class);
                startActivity(intent);
                finish();
            }
        });

        btn_Signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(tourist) {
                    fullname = String.valueOf(tie_fullname.getText());
                    username = String.valueOf(tie_username.getText());
                    password = String.valueOf(tie_password.getText());
                    email = String.valueOf(tie_email.getText());
                    phone = "NONE";
                    User db_user = new User(username, fullname, email, password);

                    if (!validateName() | !validateUsername() | !validateEmail() | !validatePassword()) {
                        return;
                    }
                    if (!fullname.equals("") && !username.equals("") && !password.equals("") && !email.equals("")) {
                        progressBar.setVisibility(View.VISIBLE);

                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(reg_mainLayout.getWindowToken(), 0);

                        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                //**************SEND VERIFICATION EMAIL***********************************************
                                if (task.isSuccessful()) {
                                    FB_user = mAuth.getCurrentUser();
                                    FB_user.sendEmailVerification().addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            userID = mAuth.getCurrentUser().getUid();
                                            FirebaseUser currentUser = mAuth.getCurrentUser();
                                            UserProfileChangeRequest profileChangeRequest = new UserProfileChangeRequest.Builder().setDisplayName(fullname).build();
                                            currentUser.updateProfile(profileChangeRequest);
                                            DocumentReference documentReference = fStore.collection("users").document(userID);
                                            Map<String, Object> user = new HashMap<>();
                                            user.put("fName", fullname);
                                            user.put("email", email);
                                            user.put("username", username);
                                            user.put("password", password);
                                            user.put("phone", phone);
                                            FirebaseDatabase.getInstance().getReference("Users").child(FirebaseAuth.getInstance().
                                                    getCurrentUser().getUid()).setValue(user);
                                            progressBar.setVisibility(View.GONE);

                                            documentReference.set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    Intent intent = new Intent(Signup.this, PermissionsActivity.class);
                                                    intent.putExtra("tourist", true);
                                                    confirm_emailDialog.show();
                                                }
                                            });
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Log.d("TAG", "onFailure: Email not sent" + e.getMessage());
                                            progressBar.setVisibility(View.GONE);
                                        }
                                    });
                                } else {
                                    if (task.getException().getMessage() == "The email address is already in use by another account.") {
                                        already_existDialog.show();
                                        progressBar.setVisibility(View.GONE);
                                    }
                                }
                            }
                        });
                    } else {
                        Toast.makeText(getApplicationContext(), "All fields are required", Toast.LENGTH_LONG).show();
                    }
                }
                else { //**************GUIDE REGISTRATION***********************************************
                    fullname = String.valueOf(tie_fullname.getText());
                    username = String.valueOf(tie_username.getText());
                    password = String.valueOf(tie_password.getText());
                    email = String.valueOf(tie_email.getText());

                    if (!validateName() | !validateUsername() | !validateEmail() | !validatePassword()) {
                        return;
                    }
                    if (!fullname.equals("") && !username.equals("") && !password.equals("") && !email.equals("")) {
                        progressBar.setVisibility(View.VISIBLE);

                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(reg_mainLayout.getWindowToken(), 0);

                        Intent i = new Intent(getApplicationContext(), GuideSignUp.class);
                        i.putExtra("username",username);
                        i.putExtra("fName",fullname);
                        i.putExtra("email",email);
                        i.putExtra("password",password);
                        startActivity(i);
                        finish();
                    }
                }
            }
        });
        inputChange();

        // ********************************************** CONFIRM EMAIL DIALOG **********************************************************

        confirm_emailDialog = new Dialog(Signup.this);
        confirm_emailDialog.setContentView(R.layout.confirm_email_dialog);
        confirm_emailDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        confirm_emailDialog.setCancelable(false);

        confirm_email_dialog_btn_Ok = confirm_emailDialog.findViewById(R.id.confirm_dialog_btn_Ok);

        confirm_email_dialog_btn_Ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), Login.class);
                startActivity(intent);
                finish();
                confirm_emailDialog.dismiss();
            }
        });

        // ********************************************** ALREADY EXIST DIALOG **********************************************************
        already_existDialog = new Dialog(Signup.this);
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
    }

    private Boolean validateName() {
        String val = String.valueOf(tie_fullname.getText());
        if (val.isEmpty()) {
            tie_fullname.setError("Full Name cannot be empty");
            return false;
        } else {
            tie_fullname.setError(null);
            return true;
        }
    }

    private Boolean validateUsername() {
        String val = String.valueOf(tie_username.getText());
        String noSpace = "\\A\\w{4,20}\\z";
        if (val.isEmpty()) {
            tie_username.setError("Username cannot be empty");
            return false;
        } else if (val.length() >= 15) {
            tie_username.setError("Username too long");
            return false;
        } else if (!val.matches(noSpace)) {
            tie_username.setError("Username should be at least 4 characters");
            return false;
        } else {
            tie_username.setError(null);
            return true;
        }
    }

    private Boolean validateEmail() {
        String val = String.valueOf(tie_email.getText());
        String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
        if (val.isEmpty()) {
            tie_email.setError("Email cannot be empty");
            return false;
        } else if (!val.matches(emailPattern)) {
            tie_email.setError("Invalid email address");
            return false;
        } else {
            tie_email.setError(null);
            return true;
        }
    }

    private Boolean validatePassword() {
        String val = String.valueOf(tie_password.getText());
        String passwordVal = "^" +
                "(?=.*[0-9])" +         //at least 1 digit
                "(?=.*[a-z])" +         //at least 1 lower case letter
                "(?=.*[A-Z])" +         //at least 1 upper case letter
                //"(?=.*[a-zA-Z])" +      //any letter
                "(?=.*[!@#$%^&+=])" +    //at least 1 special character
                "(?=\\S+$)" +           //no white spaces
                ".{8,24}" +               //at least 8 characters
                "$";
        if (val.isEmpty()) {
            tv_rule1.setTextColor(Color.RED);
            tv_rule2.setTextColor(Color.RED);
            tv_rule3.setTextColor(Color.RED);
            tv_rule4.setTextColor(Color.RED);
            return false;
        } else if (!val.matches(passwordVal)) {
            return false;
        } else {
            tie_password.setError(null);
            return true;
        }
    }

    @SuppressLint("ResourceType")
    private void PasswordCheck_UI()
    {
        String pass = String.valueOf(tie_password.getText());

        if (pass.length() >= 8) {
            isAtLeast8 = true;
            tv_rule1.setTextColor(getResources().getColor(R.color.colorAccent,null));
            frameOne.setCardBackgroundColor(Color.parseColor(getString(R.color.colorAccent)));
        } else {
            isAtLeast8 = false;
            tv_rule1.setTextColor(Color.RED);
            frameOne.setCardBackgroundColor(Color.parseColor(getString(R.color.colorDefault)));
        }
        if (pass.matches("(.*[A-Z].*)")) {
            hasUppercase = true;
            tv_rule2.setTextColor(getResources().getColor(R.color.colorAccent,null));
            frameTwo.setCardBackgroundColor(Color.parseColor(getString(R.color.colorAccent)));
        } else {
            hasUppercase = false;
            tv_rule2.setTextColor(Color.RED);
            frameTwo.setCardBackgroundColor(Color.parseColor(getString(R.color.colorDefault)));
        }
        if (pass.matches("(.*[0-9].*)")) {
            hasNumber = true;
            tv_rule3.setTextColor(getResources().getColor(R.color.colorAccent,null));
            frameThree.setCardBackgroundColor(Color.parseColor(getString(R.color.colorAccent)));
        } else {
            hasNumber = false;
            tv_rule3.setTextColor(Color.RED);
            frameThree.setCardBackgroundColor(Color.parseColor(getString(R.color.colorDefault)));
        }
        if (pass.matches("^(?=.*[_!@#$%^&=.()]).*$")) {
            hasSymbol = true;
            tv_rule4.setTextColor(getResources().getColor(R.color.colorAccent,null));
            frameFour.setCardBackgroundColor(Color.parseColor(getString(R.color.colorAccent)));
        } else {
            hasSymbol = false;
            tv_rule4.setTextColor(Color.RED);
            frameFour.setCardBackgroundColor(Color.parseColor(getString(R.color.colorDefault)));
        }
    }

    private void inputChange()
    {
        tie_password.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                PasswordCheck_UI();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }
    @Override
    public void onBackPressed() {
        Intent intent = new Intent(getApplicationContext(), Login.class);
        startActivity(intent);
        finish();
    }
}