package com.example.attendanceapp;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.UUID;

public class AddSubjectActivity extends AppCompatActivity {

    EditText etName;
    Button btnSave;
    FirebaseFirestore db;
    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_subject);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        etName = findViewById(R.id.etSubjectName);
        btnSave = findViewById(R.id.btnSaveSubject);

        btnSave.setOnClickListener(v -> {
            String name = etName.getText().toString();
            if (name.isEmpty()) return;

            String teacherId = auth.getCurrentUser().getUid();
            // Generate a random ID for the subject
            String subjectId = UUID.randomUUID().toString();

            Subject newSubject = new Subject(subjectId, name, teacherId);

            db.collection("subjects").document(subjectId).set(newSubject)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Class Created!", Toast.LENGTH_SHORT).show();
                        finish(); // Go back to dashboard
                    });
        });
    }
}