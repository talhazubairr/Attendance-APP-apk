package com.example.attendanceapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class AttendanceAdapter extends RecyclerView.Adapter<AttendanceAdapter.ViewHolder> {

    private List<StudentItem> studentList;

    public AttendanceAdapter(List<StudentItem> studentList) {
        this.studentList = studentList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_student_attendance, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        StudentItem student = studentList.get(position);
        holder.tvName.setText(student.name);
        holder.tvRoll.setText("Roll No: " + student.rollNumber);

        // Prevent bugs when scrolling
        holder.cbStatus.setOnCheckedChangeListener(null);
        holder.cbStatus.setChecked(student.isPresent);

        holder.cbStatus.setOnCheckedChangeListener((buttonView, isChecked) -> {
            student.isPresent = isChecked;
        });
    }

    @Override
    public int getItemCount() {
        return studentList.size();
    }

    // Logic for "Select All"
    public void setAllStatus(boolean status) {
        for (StudentItem item : studentList) {
            item.isPresent = status;
        }
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvRoll;
        CheckBox cbStatus;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvStudentName);
            tvRoll = itemView.findViewById(R.id.tvRollNo);
            cbStatus = itemView.findViewById(R.id.cbStatus);
        }
    }
}