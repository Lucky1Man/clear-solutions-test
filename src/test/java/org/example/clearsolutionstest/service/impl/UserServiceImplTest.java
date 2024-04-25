package org.example.clearsolutionstest.service.impl;

import jakarta.transaction.Transactional;
import jakarta.validation.ConstraintViolationException;
import org.example.clearsolutionstest.ClearSolutionsTestApplication;
import org.example.clearsolutionstest.dto.CreateUserDto;
import org.example.clearsolutionstest.dto.GetUserDto;
import org.example.clearsolutionstest.dto.UpdateUserDto;
import org.example.clearsolutionstest.entity.User;
import org.example.clearsolutionstest.repository.UserRepository;
import org.example.clearsolutionstest.service.TimeService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.ApplicationContext;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.refEq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest(classes = ClearSolutionsTestApplication.class)
class UserServiceImplTest {

    @MockBean
    UserRepository userRepository;

    @SpyBean
    TimeService timeService;

    @Autowired
    UserServiceImpl userService;

    @Autowired
    ApplicationContext context;

    @AfterEach
    void resetMocks() {
        reset(userRepository, timeService);
    }

    @Test
    void createUser_shouldPassCorrectUserToRepositoryAndReturnCorrectValue() {
        //given
        CreateUserDto createUserDto = new CreateUserDto(
                "email@gmail.com",
                String.join("", Collections.nCopies(100, "f")),
                String.join("", Collections.nCopies(100, "l")),
                LocalDate.of(2000, 1, 1),
                "Ukraine, Lviv",
                "380123123131"
        );
        User expectedUser = new User(
                null,
                createUserDto.getEmail(),
                createUserDto.getFirstName(),
                createUserDto.getLastName(),
                createUserDto.getBirthDate(),
                createUserDto.getAddress(),
                createUserDto.getPhoneNumber()
        );
        User savedUser = new User(
                UUID.randomUUID(),
                createUserDto.getEmail(),
                createUserDto.getFirstName(),
                createUserDto.getLastName(),
                createUserDto.getBirthDate(),
                createUserDto.getAddress(),
                createUserDto.getPhoneNumber()
        );
        given(userRepository.save(refEq(expectedUser, "id"))).willReturn(savedUser);
        //when
        UUID actualId = userService.createUser(createUserDto);
        //then
        assertEquals(savedUser.getId(), actualId);
        verify(userRepository, times(1)).save(expectedUser);
    }

    @Test
    void createUser_shouldThrowConstraintViolationException_ifGivenArgIsNull() {
        //then
        ConstraintViolationException ex = assertThrows(ConstraintViolationException.class, () -> userService.createUser(null));
        assertEquals("createUser.createUserDto: must not be null", ex.getMessage());
    }

    @Test
    void createUser_shouldThrowConstraintViolationExceptionContainingAllValidationExceptions_ifGivenDaoIsInvalid() {
        //given
        CreateUserDto mostInvalidCreateDto = new CreateUserDto(
                "emailgmail.com",
                String.join("", Collections.nCopies(101, "f")),
                null,
                LocalDate.of(2020, 1, 1),
                String.join("", Collections.nCopies(201, "a")),
                "380123123131234234242"
        );
        given(timeService.utcNow()).willReturn(LocalDateTime.of(2024, 1, 1, 1, 1));
        List<String> expectedExceptionMessages = List.of(
                "createUserDto.birthDate: You should be at least 18",
                "createUserDto.email: must be a well-formed email address",
                "createUserDto.phoneNumber: Phone number must be between 8 and 18 digits and contain only digits",
                "createUserDto.lastName: User must have full name",
                "createUserDto.firstName: length must be between 1 and 100",
                "createUserDto.address: length must be between 0 and 200"
        );
        //then
        ConstraintViolationException ex = assertThrows(ConstraintViolationException.class, () -> userService.createUser(mostInvalidCreateDto));
        assertTrue(
                expectedExceptionMessages.stream().allMatch(s -> ex.getMessage().contains(s)),
                "Should contain all validation exceptions"
        );
        verify(userRepository, never()).save(any());
    }

    @Test
    void transactionalSupportShouldBeTurnedOn() {
        //then
        assertTrue(UserServiceImpl.class.isAnnotationPresent(Transactional.class), "UserServiceImpl should be annotated with @Transactional");
        assertTrue(context.containsBean("transactionManager"), "Transactional support is disabled");
    }

    @Test
    void updateUser_shouldUpdateAllGivenFields() {
        //given
        UpdateUserDto updateUserDto = new UpdateUserDto(
                "email@gmail.com",
                String.join("", Collections.nCopies(100, "f")),
                String.join("", Collections.nCopies(100, "l")),
                LocalDate.of(2000, 1, 1),
                "Ukraine, Lviv",
                "380123123131"
        );
        UUID id = UUID.randomUUID();
        User mockUser = mock(User.class);
        given(userRepository.findById(id)).willReturn(Optional.of(mockUser));
        //when
        userService.updateUser(id, updateUserDto);
        //then
        verify(mockUser, times(1)).setEmail(updateUserDto.getEmail());
        verify(mockUser, times(1)).setFirstName(updateUserDto.getFirstName());
        verify(mockUser, times(1)).setLastName(updateUserDto.getLastName());
        verify(mockUser, times(1)).setBirthDate(updateUserDto.getBirthDate());
        verify(mockUser, times(1)).setAddress(updateUserDto.getAddress());
        verify(mockUser, times(1)).setPhoneNumber(updateUserDto.getPhoneNumber());
    }

    @Test
    void updateUser_shouldThrowConstraintViolationException_ifGivenArgsAreNull() {
        //then
        ConstraintViolationException ex = assertThrows(ConstraintViolationException.class, () -> userService.updateUser(null, null));
        assertTrue(ex.getMessage().contains("updateUser.id: must not be null"), "should have id is null message");
        assertTrue(ex.getMessage().contains("updateUser.updateUserDto: must not be null"), "should have updateUserDto is null message");
    }

    @Test
    void updateUser_shouldNotEffectNonSpecifiedFields() {
        //given
        UpdateUserDto updateUserDto = new UpdateUserDto(
                null,
                String.join("", Collections.nCopies(100, "f")),
                null,
                LocalDate.of(2000, 1, 1),
                null,
                "380123123131"
        );
        UUID id = UUID.randomUUID();
        User mockUser = mock(User.class);
        given(userRepository.findById(id)).willReturn(Optional.of(mockUser));
        //when
        userService.updateUser(id, updateUserDto);
        //then
        verify(mockUser, never()).setEmail(updateUserDto.getEmail());
        verify(mockUser, times(1)).setFirstName(updateUserDto.getFirstName());
        verify(mockUser, never()).setLastName(updateUserDto.getLastName());
        verify(mockUser, times(1)).setBirthDate(updateUserDto.getBirthDate());
        verify(mockUser, never()).setAddress(updateUserDto.getAddress());
        verify(mockUser, times(1)).setPhoneNumber(updateUserDto.getPhoneNumber());
    }

    @Test
    void updateUser_shouldNotEffectAnyFields_ifNoFieldsArePresent() {
        //given
        UpdateUserDto updateUserDto = new UpdateUserDto();
        UUID id = UUID.randomUUID();
        User mockUser = mock(User.class);
        given(userRepository.findById(id)).willReturn(Optional.of(mockUser));
        //when
        userService.updateUser(id, updateUserDto);
        //then
        verify(mockUser, never()).setEmail(updateUserDto.getEmail());
        verify(mockUser, never()).setFirstName(updateUserDto.getFirstName());
        verify(mockUser, never()).setLastName(updateUserDto.getLastName());
        verify(mockUser, never()).setBirthDate(updateUserDto.getBirthDate());
        verify(mockUser, never()).setAddress(updateUserDto.getAddress());
        verify(mockUser, never()).setPhoneNumber(updateUserDto.getPhoneNumber());
    }

    @Test
    void updateUser_shouldThrowIllegalArgumentsException_ifUserWithGivenIdDoesNotExist() {
        //given
        UpdateUserDto updateUserDto = new UpdateUserDto(
                "email@gmail.com",
                String.join("", Collections.nCopies(100, "f")),
                String.join("", Collections.nCopies(100, "l")),
                LocalDate.of(2000, 1, 1),
                "Ukraine, Lviv",
                "380123123131"
        );
        UUID id = UUID.randomUUID();
        given(userRepository.findById(id)).willReturn(Optional.empty());
        //then
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> userService.updateUser(id, updateUserDto));
        assertEquals("User with id '%s' not found".formatted(id), ex.getMessage());
    }

    @Test
    void updateUser_shouldThrowIllegalArgumentsException_ifUpdatedEmailAlreadyTaken() {
        //given
        UpdateUserDto updateUserDto = new UpdateUserDto(
                "email@gmail.com",
                String.join("", Collections.nCopies(100, "f")),
                String.join("", Collections.nCopies(100, "l")),
                LocalDate.of(2000, 1, 1),
                "Ukraine, Lviv",
                "380123123131"
        );
        UUID id = UUID.randomUUID();
        User mockUser = mock(User.class);
        given(userRepository.findById(id)).willReturn(Optional.of(mockUser));
        User emailTakenUser = mock(User.class);
        given(emailTakenUser.getEmail()).willReturn(updateUserDto.getEmail());
        given(userRepository.findByEmail(emailTakenUser.getEmail())).willReturn(Optional.of(emailTakenUser));
        //then
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> userService.updateUser(id, updateUserDto));
        assertEquals("User with email %s already exist".formatted(emailTakenUser.getEmail()), ex.getMessage());
        verify(mockUser, never()).setEmail(updateUserDto.getEmail());
        verify(mockUser, never()).setFirstName(updateUserDto.getFirstName());
        verify(mockUser, never()).setLastName(updateUserDto.getLastName());
        verify(mockUser, never()).setBirthDate(updateUserDto.getBirthDate());
        verify(mockUser, never()).setAddress(updateUserDto.getAddress());
        verify(mockUser, never()).setPhoneNumber(updateUserDto.getPhoneNumber());
    }

    @Test
    void updateUser_shouldThrowConstraintViolationException_ifGivenUpdateDtoIsInvalid() {
        //given
        UpdateUserDto mostInvalidCreateDto = new UpdateUserDto(
                "emailgmail.com",
                String.join("", Collections.nCopies(101, "f")),
                "",
                LocalDate.of(2020, 1, 1),
                String.join("", Collections.nCopies(201, "a")),
                "380123123131234234242"
        );
        UUID id = UUID.randomUUID();
        given(timeService.utcNow()).willReturn(LocalDateTime.of(2024, 1, 1, 1, 1));
        List<String> expectedExceptionMessages = List.of(
                "updateUserDto.birthDate: You should be at least 18",
                "updateUserDto.email: must be a well-formed email address",
                "updateUserDto.phoneNumber: Phone number must be between 8 and 18 digits and contain only digits",
                "updateUserDto.lastName: length must be between 1 and 100",
                "updateUserDto.firstName: length must be between 1 and 100",
                "updateUserDto.address: length must be between 0 and 200"
        );
        //then
        ConstraintViolationException ex = assertThrows(
                ConstraintViolationException.class,
                () -> userService.updateUser(id, mostInvalidCreateDto)
        );
        assertTrue(
                expectedExceptionMessages.stream().allMatch(s -> ex.getMessage().contains(s)),
                "Should contain all validation exceptions"
        );
        verify(userRepository, never()).findById(any());
        verify(userRepository, never()).findByEmail(any());
    }

    @Test
    void deleteUser_shouldDeleteUserWithGivenId() {
        //given
        User mockedUser = mock(User.class);
        UUID id = UUID.randomUUID();
        given(userRepository.findById(id)).willReturn(Optional.of(mockedUser));
        //when
        userService.deleteUser(id);
        //then
        verify(userRepository, times(1)).deleteById(id);
    }

    @Test
    void deleteUser_shouldThrowConstraintViolationException_ifGivenArgAreNull() {
        //then
        ConstraintViolationException ex = assertThrows(ConstraintViolationException.class, () -> userService.deleteUser(null));
        assertEquals("deleteUser.id: must not be null", ex.getMessage());
    }

    @Test
    void deleteUser_shouldNotThrowAnything_ifGivenUserIdDoesNotExist() {
        //given
        UUID id = UUID.randomUUID();
        //then
        assertDoesNotThrow(() -> userService.deleteUser(id));
        verify(userRepository, times(1)).deleteById(id);
    }

    @Test
    void findAllByBirthDateRange_shouldReturnAllDataReceivedInRepository() {
        //given
        LocalDate from = LocalDate.of(2000, 1, 1);
        LocalDate to = LocalDate.of(2000, 5, 1);
        List<User> usersFromDb = List.of(
                new User(UUID.randomUUID(), "email1@gmail.com", "first 1", "last 1",
                        LocalDate.of(2000, 1, 1), "Country 1, City 1", "3803424234242"),
                new User(UUID.randomUUID(), "email2@gmail.com", "first 2", "last 2",
                        LocalDate.of(2000, 2, 1), "Country 2, City 2", "3801243425253")
        );
        List<GetUserDto> expectedUsers = usersFromDb.stream().map(user -> new GetUserDto(
                user.getId(), user.getEmail(), user.getFirstName(), user.getLastName(), user.getBirthDate(),
                user.getAddress(), user.getPhoneNumber()
        )).toList();
        given(userRepository.getAllByBirthDateRange(from, to)).willReturn(usersFromDb);
        //when
        List<GetUserDto> actualUsers = userService.findAllByBirthDateRange(from, to);
        //then
        assertEquals(expectedUsers.size(), actualUsers.size(), "Returned value should have same size");
        assertTrue(actualUsers.containsAll(expectedUsers), "Return value should contain all users");
        verify(userRepository, times(1)).getAllByBirthDateRange(from, to);
    }

    @Test
    void findAllByBirthDateRange_shouldThrowConstraintViolationException_ifGivenArgsAreNull() {
        //then
        ConstraintViolationException ex = assertThrows(ConstraintViolationException.class, () -> userService.findAllByBirthDateRange(null, null));
        assertTrue(ex.getMessage().contains("findAllByBirthDateRange.from: must not be null"), "should have from is null message");
        assertTrue(ex.getMessage().contains("findAllByBirthDateRange.to: must not be null"), "should have to is null message");
    }

    @Test
    void findAllByBirthDateRange_shouldThrowIllegalArgumentException_ifGivenFromDateIsAfterToDate() {
        //given
        LocalDate from = LocalDate.of(2000, 6, 1);
        LocalDate to = LocalDate.of(2000, 5, 1);
        //then
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> userService.findAllByBirthDateRange(from, to));
        assertEquals("From date is after to date", ex.getMessage());
        verify(userRepository, never()).getAllByBirthDateRange(from, to);
    }

}
