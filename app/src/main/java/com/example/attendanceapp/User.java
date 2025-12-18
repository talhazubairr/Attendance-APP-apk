package com.example.attendanceapp;

public class User {
    public String name, email, role, id;

    // Empty constructor needed for Firestore
    public User() { }

    public User(String id, String name, String email, String role) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.role = role;
    }
}