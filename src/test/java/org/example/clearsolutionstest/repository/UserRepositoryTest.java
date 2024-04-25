package org.example.clearsolutionstest.repository;

import org.example.clearsolutionstest.config.TestRepositoryConfig;
import org.example.clearsolutionstest.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ContextConfiguration;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@ContextConfiguration(classes = TestRepositoryConfig.class)
class UserRepositoryTest {

    @Autowired
    UserRepository userRepository;

    @Test
    void getAllByBirthDateRange_shouldReturnAllUsersThatHaveBirthDateInRange() {
        //given
        LocalDate from = LocalDate.of(2000, 2, 1);
        LocalDate to = LocalDate.of(2000, 5, 1);
        List<User> users = List.of(
                new User(null, "email1@gmail.com", "first 1", "last 1",
                        LocalDate.of(2000, 1, 1), "Country 1, City 1", "3803424234242"),
                new User(null, "email2@gmail.com", "first 2", "last 2",
                        LocalDate.of(2000, 2, 1), "Country 2, City 2", "3803424234242"),
                new User(null, "email3@gmail.com", "first 3", "last 3",
                        LocalDate.of(2000, 3, 1), "Country 3, City 3", "3801243425253"),
                new User(null, "email4@gmail.com", "first 4", "last 4",
                        LocalDate.of(2000, 5, 1), "Country 4, City 4", "3801243425253"),
                new User(null, "email5@gmail.com", "first 5", "last 5",
                        LocalDate.of(2000, 6, 1), "Country 5, City 5", "3803424234242")
        );
        userRepository.saveAllAndFlush(users);
        List<User> expected = users.subList(1, users.size() - 1);
        //when
        List<User> actual = userRepository.getAllByBirthDateRange(from, to);
        //then
        assertEquals(expected.size(), actual.size(), "Should have same amount of elements");
        assertTrue(actual.containsAll(expected), "Should contain all expected values");
    }

    @Test
    void getAllByBirthDateRange_shouldReturnEmptyList_ifThereAreNoUsers() {
        //given
        LocalDate from = LocalDate.of(2000, 2, 1);
        LocalDate to = LocalDate.of(2000, 5, 1);
        //when
        List<User> actual = userRepository.getAllByBirthDateRange(from, to);
        //then
        assertTrue(actual.isEmpty(), "should be empty");
    }

    @Test
    void findByEmail_shouldReturnRightUser() {
        //given
        List<User> users = List.of(
                new User(null, "email1@gmail.com", "first 1", "last 1",
                        LocalDate.of(2000, 1, 1), "Country 1, City 1", "3803424234242"),
                new User(null, "email2@gmail.com", "first 2", "last 2",
                        LocalDate.of(2000, 2, 1), "Country 2, City 2", "3803424234242"),
                new User(null, "email3@gmail.com", "first 3", "last 3",
                        LocalDate.of(2000, 3, 1), "Country 3, City 3", "3801243425253"),
                new User(null, "email4@gmail.com", "first 4", "last 4",
                        LocalDate.of(2000, 5, 1), "Country 4, City 4", "3801243425253"),
                new User(null, "email5@gmail.com", "first 5", "last 5",
                        LocalDate.of(2000, 6, 1), "Country 5, City 5", "3803424234242")
        );
        users = userRepository.saveAllAndFlush(users);
        String email = "email2@gmail.com";
        //when
        Optional<User> actual = userRepository.findByEmail(email);
        //then
        assertEquals(
                users.stream().filter(u -> u.getEmail().equals(email)).findFirst(),
                actual
        );
    }

    @Test
    void findByEmail_shouldReturnEmptyOptional_ifThereIsNoUserWithGivenEmail() {
        //given
        List<User> users = List.of(
                new User(null, "email1@gmail.com", "first 1", "last 1",
                        LocalDate.of(2000, 1, 1), "Country 1, City 1", "3803424234242"),
                new User(null, "email3@gmail.com", "first 3", "last 3",
                        LocalDate.of(2000, 3, 1), "Country 3, City 3", "3801243425253"),
                new User(null, "email4@gmail.com", "first 4", "last 4",
                        LocalDate.of(2000, 5, 1), "Country 4, City 4", "3801243425253"),
                new User(null, "email5@gmail.com", "first 5", "last 5",
                        LocalDate.of(2000, 6, 1), "Country 5, City 5", "3803424234242")
        );
        users = userRepository.saveAllAndFlush(users);
        String email = "email2@gmail.com";
        //when
        Optional<User> actual = userRepository.findByEmail(email);
        //then
        assertTrue(actual.isEmpty(), "should be empty");
    }

}
