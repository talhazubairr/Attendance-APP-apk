package com.example.attendanceapp;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class StudentSubjectAdapter extends RecyclerView.Adapter<StudentSubjectAdapter.ViewHolder> {

    private Context context;
    private List<StudentDashboardActivity.SubjectModel> list;

    public StudentSubjectAdapter(Context context, List<StudentDashboardActivity.SubjectModel> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_student_subject, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        StudentDashboardActivity.SubjectModel model = list.get(position);

        holder.tvName.setText(model.name);
        holder.tvAttended.setText("Attended: " + model.attended);
        holder.tvTotal.setText("Total: " + model.total);

        // Calculation logic
        int percent = 0;
        if (model.total > 0) {
            percent = (model.attended * 100) / model.total;
        }

        holder.progressBar.setProgress(percent);
        holder.tvPercent.setText(percent + "%");

        // Color coding: Red if below 75%, Green if above
        if (percent < 75) {
            holder.progressBar.setProgressTintList(android.content.res.ColorStateList.valueOf(Color.RED));
            holder.tvPercent.setTextColor(Color.RED);
        } else {
            holder.progressBar.setProgressTintList(android.content.res.ColorStateList.valueOf(Color.GREEN));
            holder.tvPercent.setTextColor(Color.parseColor("#4CAF50"));
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvAttended, tvTotal, tvPercent;
        ProgressBar progressBar;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvSubName);
            tvAttended = itemView.findViewById(R.id.tvAttended);
            tvTotal = itemView.findViewById(R.id.tvTotal);
            tvPercent = itemView.findViewById(R.id.tvPercentage);
            progressBar = itemView.findViewById(R.id.progressBar);
        }
    }
}