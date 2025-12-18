package com.example.attendanceapp;

public class StudentItem {
    public String uid;
    public String name;
    public String rollNumber;
    public boolean isPresent; // To track the checkbox status

    public StudentItem(String uid, String name, String rollNumber) {
        this.uid = uid;
        this.name = name;
        this.rollNumber = rollNumber;
        this.isPresent = false; // Default to absent
    }
}