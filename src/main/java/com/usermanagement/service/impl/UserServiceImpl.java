package com.usermanagement.service.impl;

import com.usermanagement.db.UserStorage;
import com.usermanagement.exception.DateCheckingException;
import com.usermanagement.exception.UserNotFoundException;
import com.usermanagement.model.User;
import com.usermanagement.service.UserService;
import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.ReflectionUtils;

@Service
public class UserServiceImpl implements UserService {
    @Value("${age.checking}")
    private int minRequiredAge;

    @Override
    public User create(User user) {
        int age = Period.between(user.getBirthDate(), LocalDate.now()).getYears();
        if (age < minRequiredAge) {
            throw new DateCheckingException(
                    "For registration you need to be at least 18 years old"
            );
        }
        UserStorage.usersDB.add(user);
        return user;
    }

    @Override
    public User updateFields(String email, Map<String, Object> fields) {
        User user = getUser(email);
        for (Map.Entry<String, Object> f : fields.entrySet()) {
            Field field = ReflectionUtils.findField(User.class, f.getKey());
            assert field != null;
            field.setAccessible(true);
            if (f.getKey().equals("birthDate")) {
                LocalDate birthDate = LocalDate.parse(f.getValue().toString());
                ReflectionUtils.setField(field, user, birthDate);
            } else {
                ReflectionUtils.setField(field, user, f.getValue());
            }
        }
        return user;
    }

    @Override
    public User updateAllFields(String email, User user) {
        User userFromDB = getUser(email);
        userFromDB.setFirstName(user.getFirstName());
        userFromDB.setLastName(user.getLastName());
        userFromDB.setBirthDate(user.getBirthDate());
        userFromDB.setAddress(user.getAddress());
        userFromDB.setPhoneNumber(user.getPhoneNumber());
        return userFromDB;
    }

    @Override
    public void deleteUser(String email) {
        User user = getUser(email);
        UserStorage.usersDB.remove(user);
    }

    @Override
    public List<User> getUsersByBirthDateRange(LocalDate from, LocalDate to) {
        if (!from.isBefore(to)) {
            throw new DateCheckingException("Please, enter valid birthdate range. Date 'from' "
            + from + " should be before date 'to' " + to);
        }
        return UserStorage.usersDB.stream()
                .filter(u -> u.getBirthDate().isAfter(from) && u.getBirthDate().isBefore(to))
                .collect(Collectors.toList());
    }

    private User getUser(String email) {
        return UserStorage.usersDB.stream()
                .filter(u -> u.getEmail().equals(email))
                .findAny()
                .orElseThrow(
                () -> new UserNotFoundException("User with email: " + email + " does not exist.")
        );
    }
}
