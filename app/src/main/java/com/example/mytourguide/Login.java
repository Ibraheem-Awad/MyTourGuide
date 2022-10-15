package com.example.mytourguide;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
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
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class Login extends AppCompatActivity {

    TextInputEditText tie_Email, tie_Password, rest_dialog_tie_Email;

    Button btn_Login, rest_dialog_btn_Cancel, rest_dialog_btn_Ok,
            verification_dialog_btn_resend, verification_dialog_btn_ok,
            wrong_dialog_btn_again, wrong_dialog_btn_rest, no_acc_dialog_btn_yes,
            no_acc_dialog_btn_no, acc_type_dialog_btn_guide, acc_type_dialog_btn_tourist,
            guide_reg_dialog_btn_ok;

    TextView tv_loginText, tv_signUpText, tv_FogotPass,guide_reg_dialog_tv_infoText;
    ProgressBar progressBar;
    FirebaseAuth mAuth;
    FirebaseUser user;
    Dialog rest_passDialog, resend_verification_Dialog,
            wrong_passDialog, no_accDialog,
            acc_typeDialog, guide_regDialog;
    ConstraintLayout mainLayout;
    FirebaseFirestore fStore;
    private static boolean tourist;
    private static String enabled;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mainLayout = findViewById(R.id.login_mainLayout);

        tie_Email = findViewById(R.id.login_tie_Email);
        tie_Password = findViewById(R.id.login_tie_Password);
        btn_Login = findViewById(R.id.login_btn_Login);
        tv_loginText = findViewById(R.id.login_tv_LoginText);
        tv_signUpText = findViewById(R.id.login_tv_SignUpText);
        tv_FogotPass = findViewById(R.id.login_tv_FogotPass);
        progressBar = findViewById(R.id.login_progress);

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        fStore = FirebaseFirestore.getInstance();

        tourist = Boolean.parseBoolean(null);
        enabled = "null";
        Log.d("TAG1", "LOGIN - onCreate: user " + user + "\n tourist = " + tourist);

        if (user != null) {
            accountType(tourist);

        } else {
            Log.d("TAG", "onAuthStateChanged:signed_out");

            tv_signUpText.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    acc_typeDialog.show();
                }
            });

            tv_FogotPass.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    rest_passDialog.show();
                }
            });

            btn_Login.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String password, email;
                    email = String.valueOf(tie_Email.getText());
                    password = String.valueOf(tie_Password.getText());

                    if (!email.equals("") && !password.equals("")) {
                        progressBar.setVisibility(View.VISIBLE);

                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(mainLayout.getWindowToken(), 0);

                        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    if (mAuth.getCurrentUser().isEmailVerified()) {
                                        //GET IF HE IS TOURIST OR NOT
                                        progressBar.setVisibility(View.GONE);
                                        String ID = mAuth.getCurrentUser().getUid();
                                        DocumentReference docIdRef = fStore.collection("guides").document(ID);
                                        docIdRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                if (task.isSuccessful()) {
                                                    DocumentSnapshot document = task.getResult();
                                                    enabled = document.getString("enabled");
                                                    if (document.exists()) {
                                                        if (document.get("username") != null) {
                                                            tourist = false;
                                                            if (enabled.equals("true")) {
                                                                Log.d("TAG1", "Login - btn_Login - onComplete: " + enabled);
                                                                accountType(tourist);
                                                                Log.d("TAG1", "Login - onComplete: field does not exist " + "tourist = false");
                                                            } else {
                                                                guide_regDialog.show();
                                                            }
                                                        }
                                                    } else {
                                                        tourist = true;
                                                        accountType(tourist);
                                                        Log.d("TAG1", "Login - onComplete: field exists " + "tourist = true");
                                                    }
                                                }
                                            }
                                        });
                                    } else {
                                        resend_verification_Dialog.show();
                                        progressBar.setVisibility(View.GONE);
                                    }
                                } else {
                                    if (task.getException().getMessage() == "There is no user record corresponding to this identifier. The user may have been deleted.") {
                                        no_accDialog.show();
                                        progressBar.setVisibility(View.GONE);
                                    }

                                    if (task.getException().getMessage() == "The password is invalid or the user does not have a password.") {
                                        wrong_passDialog.show();
                                        progressBar.setVisibility(View.GONE);
                                    } else {
                                        if (task.getException().getMessage() == "There is no user record corresponding to this identifier. The user may have been deleted.") {
                                            progressBar.setVisibility(View.GONE);
                                        } else {
                                            Toast.makeText(Login.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                                            progressBar.setVisibility(View.GONE);
                                        }
                                    }
                                }
                            }
                        });
                    } else {
                        Toast.makeText(getApplicationContext(), "All fields are required", Toast.LENGTH_LONG).show();
                    }
                }
            });

            // ********************************************** REST PASSWORD DIALOG **********************************************************

            rest_passDialog = new Dialog(Login.this);
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
                    if (!rest_email.equals("")) {
                        mAuth.sendPasswordResetEmail(rest_email).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(Login.this, "Rest link sent to your Email, please check your inbox.", Toast.LENGTH_LONG).show();
                                rest_passDialog.dismiss();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(Login.this, "Error! Rest link is not sent, " + e.getMessage(), Toast.LENGTH_LONG).show();
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

            // ********************************************** RESEND VERIFICATION DIALOG ****************************************************

            resend_verification_Dialog = new Dialog(Login.this);
            resend_verification_Dialog.setContentView(R.layout.resend_verification_dialog);
            resend_verification_Dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            resend_verification_Dialog.setCancelable(false);

            verification_dialog_btn_resend = resend_verification_Dialog.findViewById(R.id.verification_dialog_btn_resend);
            verification_dialog_btn_ok = resend_verification_Dialog.findViewById(R.id.verification_dialog_btn_Ok);

            verification_dialog_btn_resend.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    user = mAuth.getCurrentUser();
                    user.sendEmailVerification().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(Login.this, "Verification Email has been sent", Toast.LENGTH_LONG).show();
                            resend_verification_Dialog.dismiss();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d("TAG", "onFailure: Email not sent" + e.getMessage());
                            resend_verification_Dialog.dismiss();
                        }
                    });
                    progressBar.setVisibility(View.GONE);
                }
            });

            verification_dialog_btn_ok.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    resend_verification_Dialog.dismiss();
                }
            });

            // ********************************************** WRONG PASS DIALOG ****************************************************

            wrong_passDialog = new Dialog(Login.this);
            wrong_passDialog.setContentView(R.layout.wrong_pass_dialog);
            wrong_passDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            wrong_passDialog.setCancelable(false);

            wrong_dialog_btn_again = wrong_passDialog.findViewById(R.id.wrong_dialog_btn_again);
            wrong_dialog_btn_rest = wrong_passDialog.findViewById(R.id.wrong_dialog_btn_rest);

            wrong_dialog_btn_rest.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    wrong_passDialog.dismiss();
                    rest_passDialog.show();
                }
            });

            wrong_dialog_btn_again.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    wrong_passDialog.dismiss();
                }
            });

            // ********************************************** NO ACCOUNT DIALOG ****************************************************
            no_accDialog = new Dialog(Login.this);
            no_accDialog.setContentView(R.layout.no_account_dialog);
            no_accDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            no_accDialog.setCancelable(false);

            no_acc_dialog_btn_yes = no_accDialog.findViewById(R.id.no_acc_dialog_btn_yes);
            no_acc_dialog_btn_no = no_accDialog.findViewById(R.id.no_acc_dialog_btn_no);

            no_acc_dialog_btn_yes.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    no_accDialog.dismiss();
                    acc_typeDialog.show();
                }
            });

            no_acc_dialog_btn_no.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    no_accDialog.dismiss();
                }
            });

            // ********************************************** ACCOUNT TYPE DIALOG ****************************************************
            acc_typeDialog = new Dialog(Login.this);
            acc_typeDialog.setContentView(R.layout.account_type_dialog);
            acc_typeDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

            acc_type_dialog_btn_guide = acc_typeDialog.findViewById(R.id.acc_type_dialog_btn_guide);
            acc_type_dialog_btn_tourist = acc_typeDialog.findViewById(R.id.acc_type_dialog_btn_tourist);

            acc_type_dialog_btn_guide.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    acc_typeDialog.dismiss();
                    Intent intent = new Intent(Login.this, Signup.class);
                    intent.putExtra("tourist", false);
                    startActivity(intent);
                    finish();
                }
            });

            acc_type_dialog_btn_tourist.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    acc_typeDialog.dismiss();
                    Intent intent = new Intent(Login.this, Signup.class);
                    intent.putExtra("tourist", true);
                    startActivity(intent);
                    finish();
                }
            });

            // ********************************************** GUIDE REGISTER DIALOG **********************************************************
            guide_regDialog = new Dialog(Login.this);
            guide_regDialog.setContentView(R.layout.guide_reg_dialog);
            guide_regDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            guide_regDialog.setCancelable(false);

            guide_reg_dialog_btn_ok = guide_regDialog.findViewById(R.id.guide_reg_dialog_btn_ok);
            guide_reg_dialog_tv_infoText = guide_regDialog.findViewById(R.id.guide_reg_dialog_tv_infoText);

            guide_reg_dialog_tv_infoText.setText("Please allow 1-2 days to let the support team confirm your information");

            guide_reg_dialog_btn_ok.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mAuth.signOut();
                    guide_regDialog.dismiss();
                }
            });
        }
    }

    private void accountType(boolean tourist) {
        Intent i = new Intent(Login.this, PermissionsActivity.class);
        String ID = mAuth.getCurrentUser().getUid();
        if (!tourist) {
            DocumentReference docIdRef = fStore.collection("guides").document(ID);
            docIdRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        enabled = document.getString("enabled");
                        if (document.exists()) {
                            if (enabled.equals("true")) {
                                i.putExtra("tourist", false);
                                Log.d("TAG1", "Login - accountType: " + tourist);
                                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(i);
                                finish();
                            } else {
                                mAuth = FirebaseAuth.getInstance();
                                mAuth.signOut();
                                Toast.makeText(Login.this, "Your account is still under review, please restart the app if you want to login again, " +
                                        "and make sure you have confirmed your email", Toast.LENGTH_LONG).show();
                            }
                        } else {
                            i.putExtra("tourist", true);
                            Log.d("TAG1", "Login - accountType: " + tourist);
                            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(i);
                            finish();
                        }
                    }
                }
            });
        } else {
            i.putExtra("tourist", true);
            Log.d("TAG1", "Login - accountType: " + tourist);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
            finish();
        }
    }
}