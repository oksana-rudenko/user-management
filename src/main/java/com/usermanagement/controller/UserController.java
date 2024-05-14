package com.usermanagement.controller;

import com.usermanagement.model.User;
import com.usermanagement.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
@Tag(name = "User management", description = "Endpoints for managing users")
public class UserController {
    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new user",
            description = "Create a new user, valid user should be at least 18 years old")
    public User create(@RequestBody @Valid User user) {
        return userService.create(user);
    }

    @PatchMapping("/{email}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Update user's fields", description = "Update one or more user's fields")
    public User updateFields(@PathVariable String email, @RequestBody Map<String, Object> fields) {
        return userService.updateFields(email, fields);
    }

    @PutMapping("/{email}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Update user", description = "Update all user's fields")
    public User update(@PathVariable String email, @RequestBody User user) {
        return userService.updateAllFields(email, user);
    }

    @DeleteMapping("/{email}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete user", description = "Delete user by email")
    public void delete(@PathVariable String email) {
        userService.deleteUser(email);
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Find users in the birthdate range",
            description = "Get all users which birthdate is in a given range")
    public List<User> getByBirthDateRange(@RequestParam String from, @RequestParam String to) {
        LocalDate dateFrom = LocalDate.parse(from);
        LocalDate dateTo = LocalDate.parse(to);
        return userService.getUsersByBirthDateRange(dateFrom, dateTo);
    }
}
