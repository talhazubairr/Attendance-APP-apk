package com.example.attendanceapp;

import android.content.Intent; // Import needed for redirection
import android.os.Bundle;
import android.widget.ImageView; // Import needed for the logout button
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.List;

public class StudentDashboardActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    FirebaseFirestore db;
    String currentUid;
    StudentSubjectAdapter adapter;
    List<SubjectModel> dashboardList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_dashboard);

        db = FirebaseFirestore.getInstance();

        // Safety check: If user is somehow null, go back to login
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }
        currentUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        recyclerView = findViewById(R.id.rvStudentSubjects);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        dashboardList = new ArrayList<>();
        adapter = new StudentSubjectAdapter(this, dashboardList);
        recyclerView.setAdapter(adapter);

        // Load Data
        loadMySubjects();

        // --- NEW: LOGOUT LOGIC ---
        // Find the logout button we added in the XML
        ImageView btnLogout = findViewById(R.id.btnLogout);

        btnLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut(); // Sign out from Firebase
            Toast.makeText(StudentDashboardActivity.this, "Logged Out", Toast.LENGTH_SHORT).show();

            // Redirect to Login Screen and clear back stack
            Intent intent = new Intent(StudentDashboardActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void loadMySubjects() {
        db.collection("subjects")
                .whereArrayContains("enrolledStudents", currentUid)
                .get()
                .addOnSuccessListener(querySnapshots -> {
                    dashboardList.clear();
                    for (DocumentSnapshot doc : querySnapshots) {
                        String subId = doc.getId();
                        String subName = doc.getString("subjectName");
                        long totalLectures = doc.getLong("totalLectures") != null ? doc.getLong("totalLectures") : 0;

                        countAttendanceForSubject(subId, subName, (int) totalLectures);
                    }
                });
    }

    private void countAttendanceForSubject(String subId, String subName, int total) {
        db.collection("attendance")
                .whereEqualTo("subjectId", subId)
                .whereArrayContains("presentStudentIds", currentUid)
                .get()
                .addOnSuccessListener(querySnapshots -> {
                    int attended = querySnapshots.size();
                    dashboardList.add(new SubjectModel(subName, attended, total));
                    adapter.notifyDataSetChanged();
                });
    }

    public static class SubjectModel {
        public String name;
        public int attended;
        public int total;

        public SubjectModel(String name, int attended, int total) {
            this.name = name;
            this.attended = attended;
            this.total = total;
        }
    }
}