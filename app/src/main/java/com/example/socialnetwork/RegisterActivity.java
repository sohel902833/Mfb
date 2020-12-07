package com.example.socialnetwork;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class RegisterActivity extends AppCompatActivity {

    private  EditText userEmail,userPassword,userConfirmPassword;
    private  Button createAccountButton;
    private  ProgressDialog loadingBar;

    private   FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth=FirebaseAuth.getInstance();

        userEmail=findViewById(R.id.register_Email);
        userPassword=findViewById(R.id.register_Password);
        userConfirmPassword=findViewById(R.id.register_ConfirmPassword);
        createAccountButton=findViewById(R.id.register_Create_Account);


        loadingBar=new ProgressDialog(this);

        createAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    createNewAccount();
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
    private void sendUserToMainActivity() {
        Intent intent=new Intent(RegisterActivity.this,MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();

    }
    private void createNewAccount() {
        String email=userEmail.getText().toString();
        String password=userPassword.getText().toString();
        String confirmPassword=userConfirmPassword.getText().toString();


        if(TextUtils.isEmpty(email)){
            userEmail.setError("Please Enter Your Email");
            userEmail.requestFocus();
        }
       else  if(TextUtils.isEmpty(password)){
            userPassword.setError("Please Enter Your Password");
            userPassword.requestFocus();
        }

       else  if(TextUtils.isEmpty(confirmPassword)){
            userConfirmPassword.setError("Please  Confirm Your Password");
            userConfirmPassword.requestFocus();
        }else if(!password.equals(confirmPassword)){
            Toast.makeText(this, "Your Password Do not match with your confirm password", Toast.LENGTH_SHORT).show();
        }else{
           loadingBar.setTitle("Creating New Account");
           loadingBar.setMessage("Please Wait . While we are creating your new account");
           loadingBar.show();
           loadingBar.setCanceledOnTouchOutside(true);

            mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            loadingBar.dismiss();
                            Toast.makeText(RegisterActivity.this, "Authenticated Successfully", Toast.LENGTH_SHORT).show();
                            sendUserToSetupActivity();


                        } else{
                            loadingBar.dismiss();
                            String message=task.getException().getMessage();
                            Toast.makeText(RegisterActivity.this, "Error Occurred "+message, Toast.LENGTH_SHORT).show();
                        }
                }
            });
        }

    }

    private void sendUserToSetupActivity() {

        Intent intent=new Intent(RegisterActivity.this,SetupActivity.class);
          intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
         finish();


    }
}