package com.lostandfound.db;

import com.lostandfound.dao.UserDao;
import com.lostandfound.model.User;
import com.lostandfound.util.PasswordUtil;

public class DatabaseSeeder {
    public static void seed() {
        UserDao userDao = new UserDao();
        if (!userDao.emailExists("admin@campus.edu")) {
            User admin = new User();
            admin.setName("System Admin");
            admin.setEmail("admin@campus.edu");
            admin.setStudentId(null);
            admin.setPasswordHash(PasswordUtil.hashPassword("admin123"));
            admin.setRole("ADMIN");
            admin.setSecurityQuestion("What is the admin recovery code?");
            admin.setSecurityAnswerHash(PasswordUtil.hashPassword("recovery123"));

            boolean success = userDao.insertUser(admin);
            if (success) {
                System.out.println("Admin user seeded! Email: admin@campus.edu | Password: admin123");
            }
        } else {
            System.out.println("Admin already exists, skipping seed.");
        }
    }
}