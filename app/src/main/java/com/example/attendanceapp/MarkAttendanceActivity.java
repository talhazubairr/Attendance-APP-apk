package com.example.attendanceapp;

import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.InputType;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MarkAttendanceActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    CheckBox cbSelectAll;
    Button btnSubmit;
    TextView tvSubject, tvDate;
    ImageView btnBack, btnAddStudent, btnCalendar;

    FirebaseFirestore db;
    String subjectId, subjectName;
    List<StudentItem> studentItems;
    AttendanceAdapter adapter;

    // Variables for Date & Edit Mode
    Calendar calendar;
    SimpleDateFormat dateFormat;
    String selectedDateString;
    String existingDocumentId = null; // Stores ID if we are editing an old record

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mark_attendance);

        db = FirebaseFirestore.getInstance();
        subjectId = getIntent().getStringExtra("SUBJECT_ID");
        subjectName = getIntent().getStringExtra("SUBJECT_NAME");

        // Init Views
        recyclerView = findViewById(R.id.rvAttendance);
        cbSelectAll = findViewById(R.id.cbSelectAll);
        btnSubmit = findViewById(R.id.btnSubmit);
        tvSubject = findViewById(R.id.tvHeaderSubject);
        tvDate = findViewById(R.id.tvDate);
        btnBack = findViewById(R.id.btnBack);
        btnAddStudent = findViewById(R.id.btnAddStudent);
        btnCalendar = findViewById(R.id.btnCalendar); // New Calendar Button

        if(subjectName != null) tvSubject.setText(subjectName);

        // Date Setup
        calendar = Calendar.getInstance();
        dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.US);
        selectedDateString = dateFormat.format(calendar.getTime());
        tvDate.setText(selectedDateString);

        studentItems = new ArrayList<>();
        adapter = new AttendanceAdapter(studentItems);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // Load students first
        loadStudents();

        // Listeners
        btnBack.setOnClickListener(v -> finish());
        btnAddStudent.setOnClickListener(v -> showAddStudentDialog());
        cbSelectAll.setOnCheckedChangeListener((buttonView, isChecked) -> adapter.setAllStatus(isChecked));

        // CALENDAR CLICK
        btnCalendar.setOnClickListener(v -> showDatePicker());

        btnSubmit.setOnClickListener(v -> saveOrUpdateAttendance());
    }

    private void showDatePicker() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> {
                    calendar.set(year, month, dayOfMonth);
                    selectedDateString = dateFormat.format(calendar.getTime());
                    tvDate.setText(selectedDateString);

                    // When date changes, check if attendance already exists!
                    checkExistingAttendance();
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }

    private void checkExistingAttendance() {
        // Clear current checks while loading
        for (StudentItem s : studentItems) s.isPresent = false;
        adapter.notifyDataSetChanged();
        existingDocumentId = null;
        btnSubmit.setText("SUBMIT ATTENDANCE");

        // Query Firestore: Subject + Date
        db.collection("attendance")
                .whereEqualTo("subjectId", subjectId)
                .whereEqualTo("dateString", selectedDateString)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        // Record Found! Switch to Edit Mode
                        existingDocumentId = querySnapshot.getDocuments().get(0).getId();
                        btnSubmit.setText("UPDATE ATTENDANCE");

                        List<String> presentIDs = (List<String>) querySnapshot.getDocuments().get(0).get("presentStudentIds");

                        // Mark students as present based on the old record
                        if (presentIDs != null) {
                            for (StudentItem student : studentItems) {
                                if (presentIDs.contains(student.uid)) {
                                    student.isPresent = true;
                                }
                            }
                            adapter.notifyDataSetChanged();
                        }
                        Toast.makeText(this, "Loaded existing attendance", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showAddStudentDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add Student");
        builder.setMessage("Enter email:");
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        builder.setView(input);
        builder.setPositiveButton("Add", (dialog, which) -> {
            String email = input.getText().toString().trim();
            if (!email.isEmpty()) addStudentByEmail(email);
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void addStudentByEmail(String email) {
        db.collection("users").whereEqualTo("email", email).get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        String uid = querySnapshot.getDocuments().get(0).getId();
                        db.collection("subjects").document(subjectId)
                                .update("enrolledStudents", FieldValue.arrayUnion(uid))
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(this, "Student Added!", Toast.LENGTH_SHORT).show();
                                    loadStudents();
                                });
                    } else {
                        Toast.makeText(this, "Email not found", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loadStudents() {
        if(subjectId == null) return;
        db.collection("subjects").document(subjectId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        List<String> studentIds = (List<String>) documentSnapshot.get("enrolledStudents");
                        if (studentIds != null && !studentIds.isEmpty()) {
                            studentItems.clear();
                            fetchStudentDetails(studentIds);
                        }
                    }
                });
    }

    private void fetchStudentDetails(List<String> studentIds) {
        // Recursive or Loop fetch - keeping it simple with loop
        for (String uid : studentIds) {
            db.collection("users").document(uid).get().addOnSuccessListener(doc -> {
                String name = doc.getString("name");
                String roll = doc.getString("rollNo");
                studentItems.add(new StudentItem(uid, name, roll == null ? "N/A" : roll));
                adapter.notifyDataSetChanged();

                // After loading students, check if we need to mark them present (for Today)
                if (studentItems.size() == studentIds.size()) {
                    checkExistingAttendance();
                }
            });
        }
    }

    private void saveOrUpdateAttendance() {
        List<String> presentStudentIds = new ArrayList<>();
        for (StudentItem item : studentItems) {
            if (item.isPresent) presentStudentIds.add(item.uid);
        }

        Map<String, Object> attendanceRecord = new HashMap<>();
        attendanceRecord.put("subjectId", subjectId);
        attendanceRecord.put("date", calendar.getTime());
        attendanceRecord.put("dateString", selectedDateString); // Important for Querying!
        attendanceRecord.put("presentStudentIds", presentStudentIds);

        if (existingDocumentId == null) {
            // CREATE NEW
            db.collection("attendance").add(attendanceRecord)
                    .addOnSuccessListener(doc -> {
                        // Only increment total lectures on NEW records
                        db.collection("subjects").document(subjectId)
                                .update("totalLectures", FieldValue.increment(1));
                        Toast.makeText(this, "Attendance Saved!", Toast.LENGTH_SHORT).show();
                        finish();
                    });
        } else {
            // UPDATE EXISTING
            db.collection("attendance").document(existingDocumentId)
                    .set(attendanceRecord)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Attendance Updated!", Toast.LENGTH_SHORT).show();
                        finish();
                    });
        }
    }
}