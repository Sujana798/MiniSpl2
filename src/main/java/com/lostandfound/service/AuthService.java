package com.lostandfound.service;

import com.lostandfound.dao.UserDao;
import com.lostandfound.model.User;
import com.lostandfound.util.PasswordUtil;
import com.lostandfound.util.RecoveryCodeUtil;

public class AuthService {

    private final UserDao userDao;

    public AuthService(UserDao userDao) {
        this.userDao = userDao;
    }

    public String register(String name, String email, String studentId, String password) {

        if (name == null || name.trim().isEmpty()
                || email == null || email.trim().isEmpty()
                || password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("All fields are required.");
        }

        if (!email.contains("@") || !email.contains(".")) {
            throw new IllegalArgumentException("Please enter a valid email address.");
        }

        if (password.length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters long.");
        }

        if (userDao.emailExists(email.trim())) {
            throw new IllegalStateException("This email is already registered.");
        }

        String hashedPassword = PasswordUtil.hashPassword(password);

        String plainRecoveryCode = RecoveryCodeUtil.generateRecoveryCode();
        String hashedRecoveryCode = PasswordUtil.hashPassword(plainRecoveryCode);

        User newUser = new User();
        newUser.setName(name.trim());
        newUser.setEmail(email.trim());
        newUser.setStudentId(studentId);
        newUser.setPasswordHash(hashedPassword);
        newUser.setRole("STUDENT");
        newUser.setRecoveryCodeHash(hashedRecoveryCode);

        boolean success = userDao.insertUser(newUser);
        if (!success) {
            throw new IllegalStateException("Registration failed. Please try again.");
        }

        return plainRecoveryCode;
    }

    public User login(String email, String password) {
        if (email == null || email.trim().isEmpty()
                || password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("Email and password are required.");
        }

        User user = userDao.findByEmail(email.trim());

        if (user == null) {
            throw new IllegalArgumentException("Invalid email or password.");
        }

        if (!PasswordUtil.checkPassword(password, user.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid email or password.");
        }

        return user;
    }

    public void resetPassword(String email, String recoveryCode, String newPassword) {
        if (email == null || email.trim().isEmpty()
                || recoveryCode == null || recoveryCode.trim().isEmpty()
                || newPassword == null || newPassword.trim().isEmpty()) {
            throw new IllegalArgumentException("All fields are required.");
        }

        if (newPassword.length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters long.");
        }

        User user = userDao.findByEmail(email.trim());

        if (user == null) {
            throw new IllegalArgumentException("No account found with this email.");
        }

        boolean codeMatches = PasswordUtil.checkPassword(recoveryCode.trim(), user.getRecoveryCodeHash());

        if (!codeMatches) {
            throw new IllegalArgumentException("Invalid recovery code.");
        }

        String newHashedPassword = PasswordUtil.hashPassword(newPassword);
        boolean success = userDao.updatePassword(user.getUserId(), newHashedPassword);

        if (!success) {
            throw new IllegalStateException("Failed to reset password. Please try again.");
        }
    }
}