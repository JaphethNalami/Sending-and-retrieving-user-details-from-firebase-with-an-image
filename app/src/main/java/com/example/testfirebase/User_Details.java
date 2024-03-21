package com.example.testfirebase;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

public class User_Details extends AppCompatActivity {
    TextView username, useremail, userphone;
    ImageView userimage;
    Button logout;
    NavigationBarView navigationBar;
    private FirebaseAuth mAuth;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_user_details);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        username = findViewById(R.id.userName);
        useremail = findViewById(R.id.userEmail);
        userphone = findViewById(R.id.phoneNumber);
        logout = findViewById(R.id.logOut);
        userimage = findViewById(R.id.imageView);



        //get instance of firebase auth
        mAuth = FirebaseAuth.getInstance();

        FirebaseUser user1 = mAuth.getCurrentUser();
        String user2 = user1.getUid();

        //get instance of firebase firestore

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        //get user details from firbase firestore and set it to respective textviews
        DocumentReference docRef = db.collection("users").document(user2);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Log.d(TAG, "DocumentSnapshot data: " + document.getData());

                        username.setText(document.getString("Name"));
                        useremail.setText(document.getString("Email"));
                        userphone.setText(document.getString("Phone_Number"));
                        //using glide set user image to imageview
                        Glide.with(getApplicationContext()).load(document.getString("Image")).into(userimage);


                    } else {
                        Log.d(TAG, "No such document");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });

        //logout button
        logout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(User_Details.this, MainActivity.class));
            finish();
        });

        //display the contents of the navigation bar


    }

}