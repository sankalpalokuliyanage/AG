package com.example.agmart;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.agmart.models.UserModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;


public class RegisterActivity extends AppCompatActivity {

    Button signup;
    EditText name, email, password, conpassword;

    FirebaseAuth auth;
    FirebaseDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);


        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

        signup = findViewById(R.id.signupText);
        name = findViewById(R.id.signup_name);
        email = findViewById(R.id.signup_email);
        password = findViewById(R.id.signup_password);
        conpassword = findViewById(R.id.signup_conpassword);

        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CreateUser();
            }
        });


    }

    private void CreateUser () {
        String username = name.getText().toString();
        String useremail = email.getText().toString();
        String userpassword = password.getText().toString();
        String userconpassword = conpassword.getText().toString();

        if (TextUtils.isEmpty(username)) {
            Toast.makeText(this, "Please input your name!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(useremail)){
            Toast.makeText(this, "Please input your email!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(userpassword)) {
            Toast.makeText(this, "Please input password!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(userconpassword)) {
            Toast.makeText(this, "Please confirm your password!", Toast.LENGTH_SHORT).show();
            return;
        }





        else {

            //create user
            auth.createUserWithEmailAndPassword(useremail, userpassword).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {

                        UserModel usermodel = new UserModel(username, useremail, userpassword);
                        String id = task.getResult().getUser().getUid();
                        database.getReference().child("users").child(id).setValue(usermodel);

                        Toast.makeText(RegisterActivity.this, "Registration Successful", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(RegisterActivity.this, "Failed", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }
}