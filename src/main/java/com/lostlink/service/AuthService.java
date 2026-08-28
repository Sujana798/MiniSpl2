package com.lostlink.service;


import com.lostlink.dao.UserDAO;
import com.lostlink.model.User;
import org.mindrot.jbcrypt.BCrypt;

import java.util.Optional;

public class AuthService {

    private final UserDAO userDAO;

    public AuthService(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    public void register(String name, String email, String password, String role) {
        if (name == null || name.trim().isEmpty()
                || email == null || email.trim().isEmpty()
                || password == null || password.trim().isEmpty()
                || role == null || role.trim().isEmpty()) {
            throw new IllegalArgumentException("All fields are required.");
        }

        if (!email.contains("@") || !email.contains(".")) {
            throw new IllegalArgumentException("Please enter a valid email address.");
        }

        if (password.length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters long.");
        }

        if (!role.equals("REPORTER") && !role.equals("ADMIN_VERIFIER")) {
            throw new IllegalArgumentException("Please select a valid role.");
        }

        if (userDAO.existsByEmail(email.trim())) {
            throw new IllegalStateException("This email is already registered.");
        }

        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
        User newUser = new User(name.trim(), email.trim(), hashedPassword, role);
        userDAO.save(newUser);
    }

    public User login(String email, String password) {
        if (email == null || email.trim().isEmpty()
                || password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("Email and password are required.");
        }

        Optional<User> userOptional = userDAO.findByEmail(email.trim());

        if (userOptional.isEmpty()) {
            throw new IllegalArgumentException("Invalid email or password.");
        }

        User user = userOptional.get();

        if (!BCrypt.checkpw(password, user.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid email or password.");
        }

        return user;
    }
}
