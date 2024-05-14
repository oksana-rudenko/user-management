package com.usermanagement.controller;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.usermanagement.db.UserStorage;
import com.usermanagement.model.User;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.Month;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class UserControllerTest {
    private static final String BOB_EMAIL = "bob@gmail.com";
    private static final String INVALID_EMAIL = "alice";
    private static final String URL_TEMPLATE = "/users";
    private static MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @BeforeAll
    static void beforeAll(
            @Autowired WebApplicationContext applicationContext
    ) throws SQLException {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(applicationContext)
                .build();
    }

    @BeforeEach
    void setUp() {
        UserStorage.usersDB.clear();
    }

    @AfterAll
    static void afterAll() {
        UserStorage.usersDB.clear();
    }

    @Test
    @DisplayName("Save valid user to DB from the valid request")
    void create_validRequest_returnsValidUser() throws Exception {
        User expected = getUserBob();
        String jsonRequest = objectMapper.writeValueAsString(expected);
        mockMvc.perform(post(URL_TEMPLATE)
                .content(jsonRequest)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email", is(expected.getEmail())))
                .andExpect(jsonPath("$.firstName", is(expected.getFirstName())))
                .andExpect(jsonPath("$.lastName", is(expected.getLastName())))
                .andExpect(jsonPath("$.birthDate", is(expected.getBirthDate().toString())))
                .andExpect(jsonPath("$.address", is(expected.getAddress())))
                .andExpect(jsonPath("$.phoneNumber", is(expected.getPhoneNumber())))
                .andReturn();
    }

    @Test
    @DisplayName("Save user to DB, empty email field, returns exception")
    void create_emptyEmailField_returnsException() throws Exception {
        User user = getUserBob();
        user.setEmail("");
        String jsonRequest = objectMapper.writeValueAsString(user);
        mockMvc.perform(post(URL_TEMPLATE)
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors", hasSize(1)))
                .andExpect(jsonPath("$.errors[0]", containsString("email must not be blank")));
    }

    @Test
    @DisplayName("Save user to DB, invalid email field, returns exception")
    void create_invalidEmailField_returnsException() throws Exception {
        User user = getUserBob();
        user.setEmail(INVALID_EMAIL);
        String jsonRequest = objectMapper.writeValueAsString(user);
        mockMvc.perform(post(URL_TEMPLATE)
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors", hasSize(1)))
                .andExpect(jsonPath("$.errors[0]",
                        containsString("email must be a well-formed email address")));
    }

    @Test
    @DisplayName("Save user to DB, empty first name field, returns exception")
    void create_emptyFirstNameField_returnsException() throws Exception {
        User user = getUserBob();
        user.setFirstName("");
        String jsonRequest = objectMapper.writeValueAsString(user);
        mockMvc.perform(post(URL_TEMPLATE)
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors", hasSize(1)))
                .andExpect(jsonPath("$.errors[0]",
                        containsString("firstName must not be blank")));
    }

    @Test
    @DisplayName("Save user to DB, empty last name field, returns exception")
    void create_emptyLastNameField_returnsException() throws Exception {
        User user = getUserBob();
        user.setLastName("");
        String jsonRequest = objectMapper.writeValueAsString(user);
        mockMvc.perform(post(URL_TEMPLATE)
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors", hasSize(1)))
                .andExpect(jsonPath("$.errors[0]",
                        containsString("lastName must not be blank")));
    }

    @Test
    @DisplayName("Save user to DB, empty birthdate field, returns exception")
    void create_emptyBirthDateField_returnsException() throws Exception {
        User user = getUserBob();
        user.setBirthDate(null);
        String jsonRequest = objectMapper.writeValueAsString(user);
        mockMvc.perform(post(URL_TEMPLATE)
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors", hasSize(1)))
                .andExpect(jsonPath("$.errors[0]",
                        containsString("birthDate must not be null")));
    }

    @Test
    @DisplayName("Save user to DB, birthdate is not in the past, returns exception")
    void create_invalidBirthDateField_returnsException() throws Exception {
        User user = getUserBob();
        user.setBirthDate(LocalDate.now());
        String jsonRequest = objectMapper.writeValueAsString(user);
        mockMvc.perform(post(URL_TEMPLATE)
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors", hasSize(1)))
                .andExpect(jsonPath("$.errors[0]",
                        containsString("birthDate must be a past date")));
    }

    @Test
    @DisplayName("Update user's fields, returns updated user")
    void updateFields_validRequest_returnsUpdatedUser() throws Exception {
        User user = getUserBob();
        UserStorage.usersDB.add(user);
        String changedFirstName = "Robby";
        String changedBirthDate = "1999-05-15";
        Map<String, Object> fields = new HashMap<>();
        fields.put("firstName", changedFirstName);
        fields.put("birthDate", changedBirthDate);
        String jsonRequest = objectMapper.writeValueAsString(fields);
        mockMvc.perform(patch(URL_TEMPLATE + "/" + BOB_EMAIL)
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email", is(user.getEmail())))
                .andExpect(jsonPath("$.firstName", is(changedFirstName)))
                .andExpect(jsonPath("$.lastName", is(user.getLastName())))
                .andExpect(jsonPath("$.birthDate", is(changedBirthDate)))
                .andExpect(jsonPath("$.address", is(user.getAddress())))
                .andExpect(jsonPath("$.phoneNumber", is(user.getPhoneNumber())))
                .andReturn();
    }

    @Test
    @DisplayName("Update all user's fields, returns updated user")
    void update_validUser_returnsUpdatedUser() throws Exception {
        User user = getUserBob();
        UserStorage.usersDB.add(user);
        User expected = getChangedUserBob();
        String jsonRequest = objectMapper.writeValueAsString(expected);
        mockMvc.perform(put(URL_TEMPLATE + "/" + BOB_EMAIL)
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email", is(expected.getEmail())))
                .andExpect(jsonPath("$.firstName", is(expected.getFirstName())))
                .andExpect(jsonPath("$.lastName", is(expected.getLastName())))
                .andExpect(jsonPath("$.birthDate", is(expected.getBirthDate().toString())))
                .andExpect(jsonPath("$.address", is(expected.getAddress())))
                .andExpect(jsonPath("$.phoneNumber", is(expected.getPhoneNumber())))
                .andReturn();
    }

    @Test
    @DisplayName("Delete user by email, returns no content status code")
    void delete_validEmail_returnsNoContent() throws Exception {
        UserStorage.usersDB.add(getUserBob());
        mockMvc.perform(delete(URL_TEMPLATE + "/" + BOB_EMAIL))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Find users by birthdate range, returns list of one user")
    void getByBirthDateRange_validRange_returnsListOfOne() throws Exception {
        User expected = getUserPhil();
        UserStorage.usersDB.add(getUserPhil());
        UserStorage.usersDB.add(getUserKate());
        String range = "?from=1990-01-01&to=2000-01-01";
        mockMvc.perform(get(URL_TEMPLATE + range)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.*.email").value(expected.getEmail()))
                .andExpect(jsonPath("$.*.firstName").value(expected.getFirstName()))
                .andExpect(jsonPath("$.*.lastName").value(expected.getLastName()))
                .andExpect(jsonPath("$.*.birthDate").value(expected.getBirthDate().toString()))
                .andExpect(jsonPath("$.*.address").value(expected.getAddress()))
                .andExpect(jsonPath("$.*.phoneNumber").value(expected.getPhoneNumber()))
                .andReturn();

    }

    private User getUserBob() {
        User user = new User();
        user.setEmail("bob@gmail.com");
        user.setFirstName("Bob");
        user.setLastName("Reynolds");
        user.setBirthDate(LocalDate.of(1988, Month.SEPTEMBER, 28));
        user.setAddress("Kyiv, Shevchenka str., 45");
        user.setPhoneNumber("+380985673535");
        return user;
    }

    private User getChangedUserBob() {
        User user = new User();
        user.setEmail("bob@gmail.com");
        user.setFirstName("Bobby");
        user.setLastName("Brown");
        user.setBirthDate(LocalDate.of(1985, Month.MARCH, 13));
        user.setAddress("Kyiv, Metrolohichna str., 14");
        user.setPhoneNumber("+380995552212");
        return user;
    }

    private User getUserPhil() {
        User user = new User();
        user.setEmail("collins@gmail.com");
        user.setFirstName("Phillip");
        user.setLastName("Collins");
        user.setBirthDate(LocalDate.of(1995, Month.JANUARY, 11));
        user.setAddress("Lviv, Ploshcha Rynok, 1");
        user.setPhoneNumber("+380671113434");
        return user;
    }

    private User getUserKate() {
        User user = new User();
        user.setEmail("brown@gmail.com");
        user.setFirstName("Kate");
        user.setLastName("Brown");
        user.setBirthDate(LocalDate.of(2002, Month.JUNE, 7));
        user.setAddress("Mykolaiv, Morska str., 112");
        user.setPhoneNumber("+380662224477");
        return user;
    }
}
