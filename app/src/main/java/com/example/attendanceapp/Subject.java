package com.example.attendanceapp;

import java.util.ArrayList;

public class Subject {
    public String subjectId, subjectName, teacherId;
    public int totalLectures;
    public ArrayList<String> enrolledStudents; // Stores IDs of students in class

    public Subject() {} // Empty constructor for Firebase

    public Subject(String subjectId, String subjectName, String teacherId) {
        this.subjectId = subjectId;
        this.subjectName = subjectName;
        this.teacherId = teacherId;
        this.totalLectures = 0;
        this.enrolledStudents = new ArrayList<>();
    }
}