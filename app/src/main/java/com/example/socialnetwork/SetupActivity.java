package com.example.socialnetwork;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class SetupActivity extends AppCompatActivity {

    final static  int GALLERY_PICK=1;


    private EditText userName,userFullName,countryName;
    private  Button  saveInformationButton;
    private  CircleImageView profileImage;
    private  FirebaseAuth mAuth;
    private  DatabaseReference userRef;
    private ProgressDialog loadingBar;

    private  String currentUserId;


    private StorageReference userProfileImageRef;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);


        mAuth=FirebaseAuth.getInstance();
        currentUserId=mAuth.getCurrentUser().getUid();
        userRef= FirebaseDatabase.getInstance().getReference().child("fc").child("Users").child(currentUserId);
        userProfileImageRef=FirebaseStorage.getInstance().getReference().child("profileImages");


        userName=findViewById(R.id.setup_UserName);
        userFullName=findViewById(R.id.setup_FullName);
        countryName=findViewById(R.id.setup_Country_Name);
        saveInformationButton=findViewById(R.id.setup_Information_Button);
        profileImage=findViewById(R.id.setup_ProifleImage);

        loadingBar=new ProgressDialog(this);


        saveInformationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    saveAccountSetupInformation();
            }
        });

        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                      /*  Intent intent=new Intent();
                        intent.setAction(Intent.ACTION_GET_CONTENT);
                        intent.setType("image/*");
                        startActivityForResult(intent,GALLERY_PICK);*/
                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setAspectRatio(1,1)
                        .start(SetupActivity.this);
            }
        });
    }


    @Override
    protected void onStart() {
        super.onStart();

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if(snapshot.exists()){
                        if(snapshot.hasChild("profileImage")){
                              String image=snapshot.child("profileImage").getValue().toString();
                              Picasso.get().load(image).placeholder(R.drawable.profile).into(profileImage);

                        }else{
                            Picasso.get().load(R.drawable.profile).placeholder(R.drawable.profile).into(profileImage);

                        }
                    }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });




    }

    private void saveAccountSetupInformation() {

        String username=userName.getText().toString();
        String fullName=userFullName.getText().toString();
        String country=countryName.getText().toString();


        if(username.isEmpty()){
            userName.setError("Please Write Your Username");
            userName.requestFocus();
        }else if(fullName.isEmpty()){
            userFullName.setError("Please Write Your Full Name");
            userFullName.requestFocus();
        }else if(country.isEmpty()){
            countryName.setError("Write Your Country Name");
            countryName.requestFocus();
        }else{
            loadingBar.setTitle("Saving Information");
            loadingBar.setMessage("Please Wait . While we are saving your data.");
            loadingBar.show();
            loadingBar.setCanceledOnTouchOutside(true);

            HashMap  userMap=new HashMap();
                                userMap.put("userName",username);
                                userMap.put("fullName",fullName);
                                userMap.put("country",country);
                                userMap.put("status","Hey there i am using poster social network, developed by  Md Sohrab Hossain Sohel");
                                userMap.put("gender","none");
                                userMap.put("dob","none");
                                userMap.put("relationshipStatus","none");
             userRef.updateChildren(userMap)
                                    .addOnCompleteListener(new OnCompleteListener() {
                                        @Override
                                        public void onComplete(@NonNull Task task) {
                                                if(task.isSuccessful()){
                                                    Toast.makeText(SetupActivity.this, "Your Account is Created Successfully", Toast.LENGTH_SHORT).show();
                                                    senduserToMainActivity();
                                                    loadingBar.dismiss();
                                                }else{
                                                    String message=task.getException().getMessage();
                                                    Toast.makeText(SetupActivity.this, "Error Occurred "+message, Toast.LENGTH_SHORT).show();
                                                    loadingBar.dismiss();
                                                }
                                        }
                                    });




        }








    }

    private void senduserToMainActivity() {
        Intent intent=new Intent(SetupActivity.this,MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();

    }

    public String getFileExtension(Uri imageuri){
        ContentResolver contentResolver=getContentResolver();
        MimeTypeMap mimeTypeMap=MimeTypeMap.getSingleton();
        return  mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(imageuri));
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
/*
        if(requestCode==GALLERY_PICK && resultCode==RESULT_OK && data!=null){
            Uri imageUri=data.getData();


        }*/
        if(requestCode==CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE){
            CropImage.ActivityResult result=CropImage.getActivityResult(data);
            if(resultCode==RESULT_OK){

                loadingBar.setTitle("Profile Image");
                loadingBar.setMessage("Please Wait . While we are updating your profile image");
                loadingBar.show();
                loadingBar.setCanceledOnTouchOutside(true);


                Uri resultUri=result.getUri();
                Picasso.get().load(resultUri).into(profileImage);
                StorageReference filePath=userProfileImageRef.child(currentUserId+".jpg");

                filePath.putFile(resultUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Toast.makeText(SetupActivity.this, "Profile Image Stored Successfully. ", Toast.LENGTH_SHORT).show();
                        Task<Uri> urlTask=taskSnapshot.getStorage().getDownloadUrl();
                        while(!urlTask.isSuccessful());
                        Uri downloaduri=urlTask.getResult();

                        userRef.child("profileImage").setValue(downloaduri.toString())
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()){

                                                Intent intent=new Intent(SetupActivity.this,SetupActivity.class);
                                                startActivity(intent);


                                                Toast.makeText(SetupActivity.this, "Profile Image Uploaded Successful", Toast.LENGTH_SHORT).show();
                                                loadingBar.dismiss();
                                            }else{
                                                String message=task.getException().getMessage();
                                                Toast.makeText(SetupActivity.this, "Error Occurred "+message, Toast.LENGTH_SHORT).show();
                                                loadingBar.dismiss();
                                            }
                                       }
                                    });



                    }
                });
            }else{
                loadingBar.dismiss();
                Toast.makeText(this, "Error Occurred : Image can not Croped.Please Try again", Toast.LENGTH_SHORT).show();
            }
        }




    }
}