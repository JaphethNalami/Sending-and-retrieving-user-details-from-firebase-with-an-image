package com.example.testfirebase;

import static android.content.ContentValues.TAG;

import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import com.bumptech.glide.Glide;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Register extends AppCompatActivity {

    Button register;
    TextView login;
    EditText email, password, phone, name;
    ImageView image;
    private FirebaseAuth mAuth;
    private String imageUrl;


    ActivityResultLauncher<PickVisualMediaRequest> launcher = registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), new ActivityResultCallback<Uri>() {
        @Override
        public void onActivityResult(Uri imageUrl) {
            if (imageUrl == null) {
                Toast.makeText(Register.this, "No image Selected", Toast.LENGTH_SHORT).show();
            } else {
                Glide.with(getApplicationContext()).load(imageUrl).into(image);
                // Set the selected image URL
                Register.this.imageUrl = imageUrl.toString();
            }
        }
    });


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        register = findViewById(R.id.register);
        login = findViewById(R.id.login);
        email = findViewById(R.id.userEmail);
        password = findViewById(R.id.password);
        phone = findViewById(R.id.phoneNumber);
        name = findViewById(R.id.userName);
        image = findViewById(R.id.imageView);

        //dialog
        Dialog dialog = new MaterialAlertDialogBuilder(this)
                .setView(new ProgressBar(this))
                .setTitle("Loading")
                .setMessage("Please wait")
                .create();

        //firebase
        mAuth = FirebaseAuth.getInstance();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                launcher.launch(new PickVisualMediaRequest.Builder()
                        .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                        .build());
            }

        });


        register.setOnClickListener(v -> {

            // show dialog
            dialog.show();

            //get user email and password
            String email1 = email.getText().toString();
            String password1 = password.getText().toString();

            // validate inputs
            if (email1.isEmpty() || password1.isEmpty()) {
                dialog.dismiss();
                Toast.makeText(Register.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            } else {
                //upload profile picture
                uploadProfilePicture(Uri.parse(imageUrl));
            }

        });

        login.setOnClickListener(v -> {
            // move to login page
            startActivity(new Intent(Register.this, MainActivity.class));
        });


    }
    private void uploadProfilePicture(Uri imageUri) {
        StorageReference storageReference = FirebaseStorage.getInstance().getReference();
        StorageReference imageRef = storageReference.child("profile_images/" + UUID.randomUUID().toString());

        imageRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    // Image uploaded successfully, get the download URL
                    imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        // Update imageUrl variable with the download URL
                        imageUrl = uri.toString();
                        // After getting the download URL, proceed to save user details in Firestore
                        saveUserDetails();
                    }).addOnFailureListener(e -> {
                        // Handle the failure to get the download URL
                        Toast.makeText(Register.this, "Image upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
                })
                .addOnFailureListener(e -> {
                    // Handle the failure to upload the image
                    Toast.makeText(Register.this, "Image upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void saveUserDetails() {

        Dialog dialog = new MaterialAlertDialogBuilder(this)
                .setView(new ProgressBar(this))
                .setTitle("Loading")
                .setMessage("Please wait")
                .create();

        //get the user input
        String emailInput = email.getText().toString();
        String phoneInput = phone.getText().toString();
        String nameInput = name.getText().toString();
        String passwordInput = password.getText().toString();

        //check if the user input is empty
        if (emailInput.isEmpty() || phoneInput.isEmpty() || nameInput.isEmpty() || imageUrl == null) {
            dialog.dismiss();
            //show error message
            email.setError("Email is required");
            phone.setError("Phone number is required");
            name.setError("Name is required");
            Toast.makeText(Register.this, "Please fill in all the required fields and select a profile image.", Toast.LENGTH_SHORT).show();
        } else {
            mAuth.createUserWithEmailAndPassword(emailInput, passwordInput)
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            dialog.dismiss();
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "createUserWithEmail:success");
                            FirebaseUser user1 = mAuth.getCurrentUser();
                            String user2 = user1.getUid();
                            Toast.makeText(Register.this, "Registration successful!.", Toast.LENGTH_SHORT).show();

                            // save user details using firestore database
                            Map<String, Object> user = new HashMap<>();
                            user.put("Name", nameInput);
                            user.put("Email", emailInput);
                            user.put("Phone_Number", phoneInput);
                            user.put("Image", imageUrl);

                            FirebaseFirestore db = FirebaseFirestore.getInstance();
                            db.collection("users").document(user2)
                                    .set(user)
                                    .addOnSuccessListener(aVoid -> {
                                        Log.d(TAG, "DocumentSnapshot successfully written!");
                                        startActivity(new Intent(Register.this, User_Details.class));
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.w(TAG, "Error writing document", e);
                                        Toast.makeText(Register.this, "Failed to save user details in Firestore.", Toast.LENGTH_SHORT).show();
                                    });
                        } else {
                            dialog.dismiss();
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(Register.this, "Registration failed!.", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

}