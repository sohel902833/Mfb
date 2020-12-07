package com.example.socialnetwork;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.renderscript.Sampler;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class PostActivity extends AppCompatActivity {
    private  static  final  int GALLERY_PICK=1;

    private  Toolbar toolbar;
    private  ImageButton selectPostImage;
    private  Button updatePostButton;
    private  EditText postDiscription;
    private  Uri imageUri;
    private  String discription,currentUserId;
    private  String downloadUrl;

    private  FirebaseAuth mAuth;
    private  StorageReference postImagesReference;
    private  DatabaseReference userRef,postRef;


    private  String saveCurrentDate,saveCurrentTime,postRandomName;

    private ProgressDialog loadingBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        loadingBar=new ProgressDialog(this);
        mAuth=FirebaseAuth.getInstance();
        currentUserId=mAuth.getCurrentUser().getUid();


        postImagesReference= FirebaseStorage.getInstance().getReference().child("Post_Images");
        userRef= FirebaseDatabase.getInstance().getReference().child("fc").child("Users");
        postRef= FirebaseDatabase.getInstance().getReference().child("fc").child("Post");


        selectPostImage=findViewById(R.id.select_post_Image);
        updatePostButton=findViewById(R.id.update_Post_Button);
        postDiscription=findViewById(R.id.post_discription);


        toolbar=findViewById(R.id.update_Post_Page_Toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Update Post");

        updatePostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    validatePostInfo();
            }
        });



        selectPostImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                    openGallery();
            }
        });




    }

    private void validatePostInfo() {

         discription=postDiscription.getText().toString();
        if(imageUri==null){
            Toast.makeText(this, "Please Select Post Image...", Toast.LENGTH_SHORT).show();
        }
       else if(discription.isEmpty()){
            postDiscription.setError("Write Somethig...");
            postDiscription.requestFocus();
        }else{

           storingImageToStorage();

        }






    }

    private void storingImageToStorage() {

        loadingBar.setTitle("Adding New Post");
        loadingBar.setMessage("Please Wait . While we are updating your new post.");
        loadingBar.show();
        loadingBar.setCanceledOnTouchOutside(true);





        Calendar calForDate=Calendar.getInstance();
        SimpleDateFormat currentDate=new SimpleDateFormat("dd-MMMM-yyy");
        saveCurrentDate=currentDate.format(calForDate.getTime());

        Calendar callForTime=Calendar.getInstance();
        SimpleDateFormat currentTime=new SimpleDateFormat("HH:mm");
        saveCurrentTime=currentTime.format(callForTime.getTime());
        postRandomName=saveCurrentDate+saveCurrentTime+System.currentTimeMillis();

        StorageReference filePath=postImagesReference.child(imageUri.getLastPathSegment()+postRandomName+".jpg");

        filePath.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
              Task<Uri> urlTask=taskSnapshot.getStorage().getDownloadUrl();
                while(!urlTask.isSuccessful());
                  Uri downloaduri=urlTask.getResult();
                  downloadUrl=downloaduri.toString();
                Toast.makeText(PostActivity.this, "Image Uploaded Successfully.", Toast.LENGTH_SHORT).show();
                savinigPostInformationToDatabase();

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(PostActivity.this, "Error Occurred: "+e.getMessage().toString(), Toast.LENGTH_SHORT).show();
                loadingBar.dismiss();
            }
        });

    }

    private void savinigPostInformationToDatabase() {



            userRef.child(currentUserId).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.exists()){
                            if(snapshot.hasChild("profileImage")){
                                    String userFullName=snapshot.child("fullName").getValue().toString();
                                    String userProfileImage=snapshot.child("profileImage").getValue().toString();

                                    HashMap postMap=new HashMap();
                                    postMap.put("uid",currentUserId);
                                    postMap.put("date",saveCurrentDate);
                                    postMap.put("time",saveCurrentTime);
                                    postMap.put("description",discription);
                                    postMap.put("postImage",downloadUrl);
                                    postMap.put("profileImage",userProfileImage);
                                    postMap.put("fullName",userFullName);
                                    postMap.put("postId",currentUserId+postRandomName);

                                    postRef.child(currentUserId+postRandomName).updateChildren(postMap)
                                                .addOnCompleteListener(new OnCompleteListener() {
                                                    @Override
                                                    public void onComplete(@NonNull Task task) {
                                                            if(task.isSuccessful()){
                                                                Toast.makeText(PostActivity.this, "New Post is Updated Successfully..", Toast.LENGTH_SHORT).show();
                                                                sendUserToMainActivity();
                                                                loadingBar.dismiss();
                                                            } else{
                                                                String message=task.getException().getMessage();
                                                                Toast.makeText(PostActivity.this, "Error Occurred "+message, Toast.LENGTH_SHORT).show();
                                                                loadingBar.dismiss();
                                                            }
                                                    }
                                                });

                            }
                        }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });





    }

    private void openGallery() {

        Intent intent=new Intent();
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent,GALLERY_PICK);


    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        int id=item.getItemId();
        if(id==android.R.id.home){
                sendUserToMainActivity();
        }




        return super.onOptionsItemSelected(item);
    }

    private void sendUserToMainActivity() {
        Intent intent=new Intent(PostActivity.this,MainActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==GALLERY_PICK && resultCode==RESULT_OK && data!=null){
            imageUri=data.getData();
            Picasso.get().load(imageUri).into(selectPostImage);
        }




    }
}