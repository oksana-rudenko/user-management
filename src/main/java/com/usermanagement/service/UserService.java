package com.usermanagement.service;

import com.usermanagement.model.User;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface UserService {
    User create(User user);
    User updateFields(String email, Map<String, Object> fields);
    User updateAllFields(String email, User user);
    void deleteUser(String email);
    List<User> getUsersByBirthDateRange(LocalDate from, LocalDate to);
}
