package com.lostlink.dao;
import com.lostlink.model.User;

import java.util.Optional;
public interface UserDAO {
    void save(User user);

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);
}
