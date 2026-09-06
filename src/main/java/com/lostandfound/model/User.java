package com.lostandfound.model;

public class User {
    private int userId;
    private String name;
    private String email;
    private String studentId;
    private String passwordHash;
    private String role; // "STUDENT" or "ADMIN"
    private String recoveryCodeHash;

    public User() {
    }

    public User(int userId, String name, String email, String studentId,
                String passwordHash, String role, String recoveryCodeHash) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.studentId = studentId;
        this.passwordHash = passwordHash;
        this.role = role;
        this.recoveryCodeHash = recoveryCodeHash;
    }


    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getRecoveryCodeHash() { return recoveryCodeHash; }
    public void setRecoveryCodeHash(String recoveryCodeHash) { this.recoveryCodeHash = recoveryCodeHash; }
}