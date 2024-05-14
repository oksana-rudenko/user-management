package com.usermanagement.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import com.usermanagement.db.UserStorage;
import com.usermanagement.exception.DateCheckingException;
import com.usermanagement.exception.UserNotFoundException;
import com.usermanagement.model.User;
import com.usermanagement.service.impl.UserServiceImpl;
import java.time.LocalDate;
import java.time.Month;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@ExtendWith(MockitoExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class UserServiceImplTest {
    private static final User USER_BOB = new User();
    private static final User USER_KATE = new User();
    private static final User USER_PHIL = new User();
    private static final User USER_YOUNGER_THAN_MIN_AGE = new User();
    private static final String BOB_EMAIL = "bob@gmail.com";
    private static final String NOT_EXISTED_EMAIL = "st@gmail.com";
    @Autowired
    private UserServiceImpl userService;

    @BeforeAll
    static void beforeAll() {
        USER_BOB.setEmail("bob@gmail.com");
        USER_BOB.setFirstName("Bob");
        USER_BOB.setLastName("Reynolds");
        USER_BOB.setBirthDate(LocalDate.of(1998, Month.SEPTEMBER, 28));
        USER_BOB.setAddress("Kyiv, Shevchenka str., 45");
        USER_BOB.setPhoneNumber("+380985673535");

        USER_PHIL.setEmail("collins@gmail.com");
        USER_PHIL.setFirstName("Phillip");
        USER_PHIL.setLastName("Collins");
        USER_PHIL.setBirthDate(LocalDate.of(1995, Month.JANUARY, 11));
        USER_PHIL.setAddress("Lviv, Ploshcha Rynok, 1");
        USER_PHIL.setPhoneNumber("+380671113434");

        USER_KATE.setEmail("brown@gmail.com");
        USER_KATE.setFirstName("Kate");
        USER_KATE.setLastName("Brown");
        USER_KATE.setBirthDate(LocalDate.of(2002, Month.JUNE, 7));
        USER_KATE.setAddress("Mykolaiv, Morska str., 112");
        USER_KATE.setPhoneNumber("+380662224477");

        USER_YOUNGER_THAN_MIN_AGE.setEmail("collins@gmail.com");
        USER_YOUNGER_THAN_MIN_AGE.setFirstName("Phillip");
        USER_YOUNGER_THAN_MIN_AGE.setLastName("Collins");
        USER_YOUNGER_THAN_MIN_AGE.setBirthDate(LocalDate.of(2015, Month.JANUARY, 11));
        USER_YOUNGER_THAN_MIN_AGE.setAddress("Lviv, Ploshcha Rynok, 1");
        USER_YOUNGER_THAN_MIN_AGE.setPhoneNumber("+380671113434");
    }

    @Test
    @DisplayName("Save valid user to DB from the valid request")
    void create_validUser_returnsValidUserFromDB() {
        userService.create(USER_BOB);
        User actual = UserStorage.usersDB.get(0);
        Assertions.assertEquals(USER_BOB, actual);
        UserStorage.usersDB.clear();
    }

    @Test
    @DisplayName("Save user younger than minimum allowed age, returns DateCheckingException")
    void create_userYoungerThanMinAge_returnsException() {
        DateCheckingException exception = assertThrows(DateCheckingException.class,
                () -> userService.create(USER_YOUNGER_THAN_MIN_AGE)
        );
        String expected = "For registration you need to be at least 18 years old";
        String actual = exception.getMessage();
        Assertions.assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Update some user's fields, returns updated user")
    void updateFields_validFields_returnsUpdatedUser() {
        UserStorage.usersDB.add(USER_BOB);
        String changedFirstName = "Robby";
        String changedBirthDate = "1999-05-15";
        Map<String, Object> fields = new HashMap<>();
        fields.put("firstName", changedFirstName);
        fields.put("birthDate", changedBirthDate);
        User actual = userService.updateFields(BOB_EMAIL, fields);
        Assertions.assertEquals(BOB_EMAIL, actual.getEmail());
        Assertions.assertEquals(changedFirstName, actual.getFirstName());
        Assertions.assertEquals(USER_BOB.getLastName(), actual.getLastName());
        Assertions.assertEquals(LocalDate.parse(changedBirthDate), actual.getBirthDate());
        Assertions.assertEquals(USER_BOB.getAddress(), actual.getAddress());
        Assertions.assertEquals(USER_BOB.getPhoneNumber(), actual.getPhoneNumber());
        UserStorage.usersDB.clear();
    }

    @Test
    @DisplayName("Update user's fields by not existed email, returns UserNotFoundException")
    void updateFields_notExistedEmail_returnsException() {
        UserStorage.usersDB.add(USER_BOB);
        String changedFirstName = "Robert";
        String changedBirthDate = "1999-05-15";
        Map<String, Object> fields = new HashMap<>();
        fields.put("firstName", changedFirstName);
        fields.put("birthDate", changedBirthDate);
        UserNotFoundException exception = Assertions.assertThrows(UserNotFoundException.class,
                () -> userService.updateFields(NOT_EXISTED_EMAIL, fields)
        );
        String expected = "User with email: " + NOT_EXISTED_EMAIL + " does not exist.";
        String actual = exception.getMessage();
        Assertions.assertEquals(expected, actual);
        UserStorage.usersDB.clear();
    }

    @Test
    @DisplayName("Update user, returns user with all updated fields")
    void updateAllFields_validUser_returnsUpdatedUser() {
        UserStorage.usersDB.add(USER_BOB);
        User bob = new User();
        bob.setEmail(BOB_EMAIL);
        bob.setFirstName("Robert");
        bob.setLastName("Junior");
        bob.setBirthDate(LocalDate.parse("2000-04-27"));
        bob.setAddress("Cherkasy, Khreshchatyk 28");
        bob.setPhoneNumber("+380678889922");
        User actual = userService.updateAllFields(BOB_EMAIL, bob);
        Assertions.assertEquals(bob, actual);
        UserStorage.usersDB.clear();
    }

    @Test
    @DisplayName("Delete user by its email, user is removed from DB")
    void deleteUser_validUserEmail_removedFromDB() {
        UserStorage.usersDB.add(USER_BOB);
        userService.deleteUser(BOB_EMAIL);
        Assertions.assertEquals(0, UserStorage.usersDB.size());
    }

    @Test
    @DisplayName("Find users by birthdate range, returns list of one user")
    void getUsersByBirthDateRange_validRange_returnsListOfOne() {
        UserStorage.usersDB.add(USER_PHIL);
        UserStorage.usersDB.add(USER_KATE);
        LocalDate from = LocalDate.of(1990, Month.JANUARY, 1);
        LocalDate to = LocalDate.of(2000, Month.JANUARY, 1);
        List<User> actual = userService.getUsersByBirthDateRange(from, to);
        Assertions.assertEquals(1, actual.size());
        Assertions.assertEquals(USER_PHIL, actual.get(0));
        UserStorage.usersDB.clear();
    }

    @Test
    @DisplayName("Find users by birthdate range, range is not valid, returns DateCheckingException")
    void getUsersByBirthDateRange_invalidRange_returnsException() {
        LocalDate from = LocalDate.of(2000, Month.JANUARY, 1);
        LocalDate to = LocalDate.of(1990, Month.JANUARY, 1);
        DateCheckingException exception = Assertions.assertThrows(DateCheckingException.class,
                () -> userService.getUsersByBirthDateRange(from, to)
        );
        String expected = "Please, enter valid birthdate range. Date 'from' "
                + from + " should be before date 'to' " + to;
        String actual = exception.getMessage();
        Assertions.assertEquals(expected, actual);
    }
}
