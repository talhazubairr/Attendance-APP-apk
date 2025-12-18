package com.example.attendanceapp;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // Ensure this XML exists (see Step 2)

        // Check if user is already logged in
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser == null) {
            // No user found, go to Login after 2 seconds (Splash effect)
            new Handler().postDelayed(this::goToLogin, 2000);
        } else {
            // User found, check their role in Database
            checkRoleAndRedirect(currentUser.getUid());
        }
    }

    private void checkRoleAndRedirect(String uid) {
        FirebaseFirestore.getInstance().collection("users").document(uid).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String role = documentSnapshot.getString("role");
                        if ("Teacher".equals(role)) {
                            startActivity(new Intent(this, TeacherDashboardActivity.class));
                        } else {
                            startActivity(new Intent(this, StudentDashboardActivity.class));
                        }
                        finish(); // Prevent user from going back to Splash
                    } else {
                        // User exists in Auth but not in Database (Rare error)
                        goToLogin();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error checking role: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    goToLogin();
                });
    }

    private void goToLogin() {
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }
}