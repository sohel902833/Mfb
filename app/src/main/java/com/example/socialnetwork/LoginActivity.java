package com.example.socialnetwork;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {
    private  Button loginButton;
    private  EditText userEmail,userPassword;
    private TextView needNewAccountLink;

    private  ProgressDialog loadingBar;


    private FirebaseAuth mAuth;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth=FirebaseAuth.getInstance();
        
        
        needNewAccountLink=findViewById(R.id.register_account_link);
        userEmail=findViewById(R.id.login_Email);
        userPassword=findViewById(R.id.login_Password);
        loginButton=findViewById(R.id.login_Button);
        loadingBar=new ProgressDialog(this);




        needNewAccountLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    sendToRegisterActivity();
            }
        });


        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    loginUser();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentUser=mAuth.getCurrentUser();
        if(currentUser!=null){
            sendUserToMainActivity();
        }
    }

    private void sendToRegisterActivity() {

        Intent intent=new Intent(LoginActivity.this,RegisterActivity.class);
        startActivity(intent);
    }



    private void loginUser() {

        String email=userEmail.getText().toString();
        String password=userPassword.getText().toString();
        
        
        if(email.isEmpty()){
            userEmail.setError("Write Your Email");
            userEmail.requestFocus();
        }else if(password.isEmpty()){
            userPassword.setError("Write Your Password");
            userPassword.requestFocus();
        }else{
            loadingBar.setTitle("Logging in");
            loadingBar.setMessage("Please Wait . While we are Logging into your account.");
            loadingBar.show();
            loadingBar.setCanceledOnTouchOutside(true);

            mAuth.signInWithEmailAndPassword(email,password)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                    if(task.isSuccessful()){
                                        sendUserToMainActivity();
                                        loadingBar.dismiss();
                                        Toast.makeText(LoginActivity.this, "Logged  in successful", Toast.LENGTH_SHORT).show();
                                    } else{
                                        loadingBar.dismiss();
                                        String message=task.getException().getMessage();
                                        Toast.makeText(LoginActivity.this, "Error Occurred "+message, Toast.LENGTH_SHORT).show();

                                    }
                            }
                        });
            
            
            
            
        }


    }

    private void sendUserToMainActivity() {
        Intent intent=new Intent(LoginActivity.this,MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();

    }


}