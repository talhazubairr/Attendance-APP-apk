package com.example.attendanceapp;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.List;

public class SubjectAdapter extends RecyclerView.Adapter<SubjectAdapter.ViewHolder> {

    private List<Subject> subjectList;
    private Context context;
    private FirebaseFirestore db;

    public SubjectAdapter(Context context, List<Subject> subjectList) {
        this.context = context;
        this.subjectList = subjectList;
        this.db = FirebaseFirestore.getInstance();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_subject_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Subject subject = subjectList.get(position);
        holder.tvName.setText(subject.subjectName);
        holder.tvClasses.setText("Classes Held: " + subject.totalLectures);

        int studentCount = (subject.enrolledStudents != null) ? subject.enrolledStudents.size() : 0;
        holder.tvStudents.setText("Students: " + studentCount);

        // --- CLICK TO OPEN ATTENDANCE ---
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, MarkAttendanceActivity.class);
            intent.putExtra("SUBJECT_ID", subject.subjectId);
            intent.putExtra("SUBJECT_NAME", subject.subjectName);
            context.startActivity(intent);
        });

        // --- NEW: DELETE BUTTON LOGIC ---
        holder.btnDelete.setOnClickListener(v -> {
            new AlertDialog.Builder(context)
                    .setTitle("Delete Subject")
                    .setMessage("Are you sure you want to delete " + subject.subjectName + "?")
                    .setPositiveButton("Delete", (dialog, which) -> deleteSubject(subject, position))
                    .setNegativeButton("Cancel", null)
                    .show();
        });
    }

    private void deleteSubject(Subject subject, int position) {
        db.collection("subjects").document(subject.subjectId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    // Remove from list and update UI
                    subjectList.remove(position);
                    notifyItemRemoved(position);
                    notifyItemRangeChanged(position, subjectList.size());
                    Toast.makeText(context, "Subject Deleted", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "Error deleting: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public int getItemCount() {
        return subjectList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvStudents, tvClasses;
        ImageView btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvSubjectName);
            tvStudents = itemView.findViewById(R.id.tvStudentCount);
            tvClasses = itemView.findViewById(R.id.tvClassesHeld);
            btnDelete = itemView.findViewById(R.id.btnDeleteSubject);
        }
    }
}