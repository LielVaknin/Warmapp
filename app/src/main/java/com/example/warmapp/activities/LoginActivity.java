package com.example.warmapp.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.warmapp.R;
import com.example.warmapp.classes.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;

import java.io.ByteArrayOutputStream;
import java.util.Objects;

public class LoginActivity extends AppCompatActivity {

    private TextInputLayout lEmail;
    private TextInputLayout lPassword;
    private TextInputEditText email;
    private TextInputEditText password;
    TextView forgetPassword;
    Dialog dialog;
    MaterialButton login;
    TextView signup;
    private ProgressDialog progressDialog;

    FirebaseAuth auth;
    private String userID;
    private User user;

    @SuppressLint("UseCompatLoadingForDrawables")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        setContentView(R.layout.activity_login);

        email = findViewById(R.id.email_input_edit_text);
        lEmail = findViewById(R.id.email_input_layout);
        password = findViewById(R.id.password_input_edit_text);
        lPassword = findViewById(R.id.password_input_layout);
        forgetPassword = findViewById(R.id.forget_password_txt);
        login = findViewById(R.id.button_login);
        signup = findViewById(R.id.sign_up_txt);

        auth = FirebaseAuth.getInstance();

        //user press login button
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String txt_email = email.getText().toString();
                String txt_password = password.getText().toString();

                if (txt_email.isEmpty()){
                    lEmail.setError("Enter a email!");
                }else if (txt_password.isEmpty()){
                    lPassword.setError("Enter a password!");
                }else{
                    LoginUserAccount(txt_email,txt_password);
                }
            }
        });

        //go to sign up activity
        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this,SignUpActivity.class);
                startActivity(intent);
                finish();
            }
        });

        //forget password dialog
        dialog = new Dialog(LoginActivity.this);
        dialog.setContentView(R.layout.layout_reset_password);
        dialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.edit_text_bg));
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.setCancelable(false);
        dialog.getWindow().getAttributes().windowAnimations = R.style.animation;

        forgetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.show();
            }
        });

        TextInputLayout layoutEmail = dialog.findViewById(R.id.edit_email);
        TextInputEditText inputEditTextEmail = dialog.findViewById(R.id.input_edit_text_reset_password);

        MaterialButton reset = dialog.findViewById(R.id.btn_reset);
        reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String txt_email = inputEditTextEmail.getText().toString();

                if (txt_email.isEmpty()){
                    layoutEmail.setError("Enter a email!");
                }else{
                    emailToReset(txt_email);
                    dialog.dismiss();
                }
            }
        });
        MaterialButton cancel = dialog.findViewById(R.id.btn_cancel);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
    }

    //helper functions
    private void LoginUserAccount(String email, String password) {
        auth.signInWithEmailAndPassword(email,password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()){
                    progressDialog = new ProgressDialog(LoginActivity.this);
                    progressDialog.setMessage("Loading");
                    progressDialog.show();
                    userID = Objects.requireNonNull(auth.getCurrentUser()).getUid();
                    getUser();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(LoginActivity.this,e.getMessage(),Toast.LENGTH_LONG).show();
            }
        });
    }

    private void getUser() {
        FirebaseDatabase.getInstance().getReference("Users").child(userID)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        user = snapshot.getValue(User.class);
                        getUserImage();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void getUserImage() {
        final long ONE_MEGABYTE = 1024 * 1024;
        FirebaseStorage.getInstance().getReference().child(userID + ".jpg").getBytes(ONE_MEGABYTE)
                .addOnSuccessListener(new OnSuccessListener<byte[]>() {
                    @SuppressLint("NotifyDataSetChanged")
                    @Override
                    public void onSuccess(byte[] bytes) {
                        if (bytes == null){
                            progressDialog.dismiss();
                            Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                            intent.putExtra("firstName", user.getFirstName());
                            startActivity(intent);
                            finish();
                        }
                        Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                        ByteArrayOutputStream stream = new ByteArrayOutputStream();
                        bmp.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                        byte[] byteArray = stream.toByteArray();
                        progressDialog.dismiss();
                        Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                        intent.putExtra("firstName", user.getFirstName());
                        intent.putExtra("userImage",byteArray);
                        startActivity(intent);
                        finish();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
            }
        });
    }

    private void emailToReset(String email) {
        auth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    Toast.makeText(LoginActivity.this,"Rest password mail send to: "+ email,Toast.LENGTH_LONG).show();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(LoginActivity.this,e.getMessage(),Toast.LENGTH_LONG).show();
            }
        });

    }
}