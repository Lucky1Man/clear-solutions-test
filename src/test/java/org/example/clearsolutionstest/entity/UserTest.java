package org.example.clearsolutionstest.entity;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import net.bytebuddy.utility.RandomString;
import org.example.clearsolutionstest.config.TestValidationConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(classes = {TestValidationConfig.class})
class UserTest {

    @Autowired
    Validator validator;

    User user;

    @BeforeEach
    void init() {
        user = User.builder()
                .withId(UUID.randomUUID())
                .withEmail("email@gmail.com")
                .withFirstName("f")
                .withLastName("l")
                .withBirthDate(LocalDate.of(2000, 1, 1))
                .withAddress("Ukraine, Lviv")
                .withPhoneNumber("38087943243")
                .build();
    }

    @Test
    void validEntityCase() {
        //when
        Set<ConstraintViolation<User>> violationExceptions = validator.validate(user);
        //then
        assertTrue(violationExceptions.isEmpty(), "Valid object should not contain any violations");
    }

    private static Stream<Arguments> emailConstraintRecourses() {
        String nullEmail = null;
        return Stream.of(
                Arguments.of(nullEmail),
                Arguments.of("@gmail.com"),
                Arguments.of("asdsad111@.com"),
                Arguments.of("asdsad111.com"),
                Arguments.of("asdsad111@aaa."),
                Arguments.of("111111111@.com")
        );
    }

    @ParameterizedTest
    @MethodSource("emailConstraintRecourses")
    void emailConstraint_invalidCases(String email) {
        //given
        user.setEmail(email);
        //when
        Set<ConstraintViolation<User>> result = validator.validate(user);
        //then
        assertTrue(
                result.stream()
                        .anyMatch(violation ->
                                violation.getMessage().equals(
                                        email != null ?
                                                "must be a well-formed email address" :
                                                "User must have email"
                                )
                        )
        );
    }

    private static Stream<Arguments> nameConstraintResources() {
        String nullName = null;
        String nameShort = "";
        String nameLong = new RandomString(101).nextString();
        return Stream.of(
                Arguments.of(nullName, "User must have %s name"),
                Arguments.of(nameShort, "length must be between 1 and 100"),
                Arguments.of(nameLong, "length must be between 1 and 100")
        );
    }

    @ParameterizedTest
    @MethodSource("nameConstraintResources")
    void firstNameConstraint_invalidCases(String name, String expectedMessage) {
        //given
        user.setFirstName(name);
        //when
        Set<ConstraintViolation<User>> result = validator.validate(user);
        //then
        assertTrue(
                result.stream()
                        .anyMatch(violation -> violation.getMessage().equals(expectedMessage.formatted("first")))
        );
    }

    @ParameterizedTest
    @MethodSource("nameConstraintResources")
    void lastNameConstraint_invalidCases(String name, String expectedMessage) {
        //given
        user.setLastName(name);
        //when
        Set<ConstraintViolation<User>> result = validator.validate(user);
        //then
        assertTrue(
                result.stream()
                        .anyMatch(violation -> violation.getMessage().equals(expectedMessage.formatted("last")))
        );
    }

    @Test
    void dateOfBirthConstraint_isNullCase() {
        //given
        user.setBirthDate(null);
        //when
        Set<ConstraintViolation<User>> result = validator.validate(user);
        //then
        assertTrue(
                result.stream()
                        .anyMatch(violation -> violation.getMessage().equals(
                                "User must have birth date"
                        ))
        );
    }

    @Test
    void dateOfBirthConstraint_inFutureCase() {
        //given
        user.setBirthDate(LocalDate.of(999999999, 12, 28));
        //when
        Set<ConstraintViolation<User>> result = validator.validate(user);
        //then
        assertTrue(
                result.stream()
                        .anyMatch(violation -> violation.getMessage().equals(
                                "must be a past date"
                        ))
        );
    }

    @Test
    void addressConstraint_tooLongCase() {
        //given
        user.setAddress(new RandomString(201).nextString());
        //when
        Set<ConstraintViolation<User>> result = validator.validate(user);
        //then
        assertTrue(
                result.stream()
                        .anyMatch(violation -> violation.getMessage().equals(
                                "length must be between 0 and 200"
                        ))
        );
    }

    private static Stream<Arguments> phoneNumberConstraintResources() {
        return Stream.of(
                Arguments.of("7777777"),
                Arguments.of("1922222222222222222"),
                Arguments.of("1922222ASd2222"),
                Arguments.of("19222 2222222"),
                Arguments.of("19222_2222222"),
                Arguments.of("19222+2222222")
        );
    }

    @ParameterizedTest
    @MethodSource("phoneNumberConstraintResources")
    void phoneNumberConstraint_invalidCases(String name) {
        //given
        user.setPhoneNumber(name);
        //when
        Set<ConstraintViolation<User>> result = validator.validate(user);
        //then
        assertTrue(
                result.stream()
                        .anyMatch(violation -> violation.getMessage().equals("Phone number must be between 8 and 18 digits"))
        );
    }

    @Test
    void toString_shouldContainAllFields() {
        //then
        assertEquals(
                "User(id=%s, email=%s, firstName=%s, lastName=%s, birthDate=%s, address=%s, phoneNumber=%s)"
                        .formatted(user.getId(), user.getEmail(), user.getFirstName(), user.getLastName(), user.getBirthDate(),
                                user.getAddress(), user.getPhoneNumber()),
                user.toString()
        );
    }

}
