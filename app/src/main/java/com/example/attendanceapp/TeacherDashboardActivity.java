package com.example.attendanceapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView; // Import for the logout icon
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.List;

public class TeacherDashboardActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    FloatingActionButton fabAdd;
    ImageView btnLogout; // Declare the logout button

    FirebaseFirestore db;
    FirebaseAuth auth;
    List<Subject> subjectList;
    SubjectAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_dashboard);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        recyclerView = findViewById(R.id.recyclerViewSubjects);
        fabAdd = findViewById(R.id.fabAdd);
        btnLogout = findViewById(R.id.btnLogout); // Find the logout button from XML

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        subjectList = new ArrayList<>();
        adapter = new SubjectAdapter(this, subjectList);
        recyclerView.setAdapter(adapter);

        // Load subjects for THIS teacher
        loadSubjects();

        // Add Button Click (Create New Class)
        fabAdd.setOnClickListener(v -> {
            startActivity(new Intent(TeacherDashboardActivity.this, AddSubjectActivity.class));
        });

        // --- NEW: LOGOUT LOGIC ---
        btnLogout.setOnClickListener(v -> {
            auth.signOut(); // Sign out from Firebase
            Toast.makeText(TeacherDashboardActivity.this, "Logged Out", Toast.LENGTH_SHORT).show();

            // Go back to Login Screen
            Intent intent = new Intent(TeacherDashboardActivity.this, LoginActivity.class);
            // Clear the back stack so user can't press "Back" to return here
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void loadSubjects() {
        if (auth.getCurrentUser() == null) return;

        String uid = auth.getCurrentUser().getUid();

        db.collection("subjects")
                .whereEqualTo("teacherId", uid) // Only show MY subjects
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    subjectList.clear();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        Subject s = doc.toObject(Subject.class);
                        subjectList.add(s);
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error loading data", Toast.LENGTH_SHORT).show();
                });
    }
}