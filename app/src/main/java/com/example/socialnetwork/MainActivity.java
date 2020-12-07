package com.example.socialnetwork;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.renderscript.Sampler;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity {
    private NavigationView navigationView;
    private DrawerLayout drawerLayout;
    private RecyclerView recyclerView;
    private  CircleImageView navProfileImage;
    private  TextView navProfileUserName;
    private ImageButton addNewPostButton;
    private Toolbar mToolbar;
    private ActionBarDrawerToggle actionBarDrawerToggle;


    private  FirebaseAuth mAuth;
    private  DatabaseReference userRef;
    private String currentUserid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth=FirebaseAuth.getInstance();
        if(mAuth.getCurrentUser()!=null){
            currentUserid=mAuth.getCurrentUser().getUid();
        }
       userRef= FirebaseDatabase.getInstance().getReference().child("fc").child("Users");

        mToolbar=findViewById(R.id.main_page_Toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Home");

        addNewPostButton=findViewById(R.id.add_new_post_button);



        drawerLayout=findViewById(R.id.drawerlayout);
        actionBarDrawerToggle=new ActionBarDrawerToggle(this,drawerLayout,R.string.drawer_open,R.string.drawer_close);

        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        navigationView=findViewById(R.id.navigation_view);


        View navView=navigationView.inflateHeaderView(R.layout.navigation_header);

        navProfileImage=navView.findViewById(R.id.nav_profile_image);
        navProfileUserName=navView.findViewById(R.id.nav_user_full_name);

        getUserData();

        
        
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                UserMenuSelector(menuItem);
                return false;
            }
        });
        
        addNewPostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    sendUserToPostActivity();
            }
        });
        
        
        
        
        
        
        
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentUser=mAuth.getCurrentUser();
        if(currentUser==null){
            sendUserToLoginActivity();
        }else{
         CheckUserExistance();
        }

    }

    private void getUserData() {
        userRef.child(currentUserid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    if(snapshot.hasChild("profileImage") && snapshot.hasChild("fullName")){
                        String image=snapshot.child("profileImage").getValue().toString();
                        String fullName=snapshot.child("fullName").getValue().toString();

                        Picasso.get().load(image).placeholder(R.drawable.profile).into(navProfileImage);
                        navProfileUserName.setText(fullName);


                    }else if(snapshot.hasChild("fullName")){
                        String fullName=snapshot.child("fullName").getValue().toString();
                        navProfileUserName.setText(fullName);

                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void sendUserToPostActivity(){

        Intent intent=new Intent(MainActivity.this,PostActivity.class);
       startActivity(intent);
    }

    public void sendUserToLoginActivity(){

        Intent intent=new Intent(MainActivity.this,LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }




    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if(actionBarDrawerToggle.onOptionsItemSelected(item)){
            return  true;
        }


        return super.onOptionsItemSelected(item);
    }

    private void UserMenuSelector(MenuItem menuItem) {

        switch (menuItem.getItemId()){
            case R.id.nav_profile:
                Toast.makeText(this, "Profile", Toast.LENGTH_SHORT).show();
                break;
            case R.id.nav_home:
                Toast.makeText(this, "Home", Toast.LENGTH_SHORT).show();
                break;
            case R.id.nav_friends:
                Toast.makeText(this, "Friends", Toast.LENGTH_SHORT).show();
                break;
            case R.id.nav_find_friends:
                Toast.makeText(this, "Find Friends", Toast.LENGTH_SHORT).show();
                break;
            case R.id.nav_messages:
                Toast.makeText(this, "Messages", Toast.LENGTH_SHORT).show();
                break;
            case R.id.nav_settings:
                Toast.makeText(this, "Settings", Toast.LENGTH_SHORT).show();
                break;
            case R.id.nav_logout:{
                mAuth.signOut();
                sendUserToLoginActivity();
                break;
            }
            case R.id.nav_post:{
                sendUserToPostActivity();
            }

        }
    }


    public  void CheckUserExistance() {

        final String current_user_id=mAuth.getCurrentUser().getUid();
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if(!snapshot.hasChild(current_user_id)){
                       sendUsertoSetupActivity();
                    }else if(!snapshot.child(current_user_id).hasChild("userName")){
                        sendUsertoSetupActivity();
                    }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });



    }

    private void sendUsertoSetupActivity() {

        Intent intent=new Intent(MainActivity.this,SetupActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();



    }

}