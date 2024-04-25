package org.example.clearsolutionstest.service.impl;

import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.clearsolutionstest.dto.CreateUserDto;
import org.example.clearsolutionstest.dto.GetUserDto;
import org.example.clearsolutionstest.dto.UpdateUserDto;
import org.example.clearsolutionstest.entity.User;
import org.example.clearsolutionstest.repository.UserRepository;
import org.example.clearsolutionstest.service.UserService;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    private final ModelMapper modelMapper;

    @Override
    public UUID createUser(@Valid @NotNull CreateUserDto createUserDto) {
        log.debug("createUser {}", createUserDto);
        User user = modelMapper.map(createUserDto, User.class);
        UUID id = userRepository.save(user).getId();
        log.debug("createUser saved {}", user);
        log.debug("end createUser {}", id);
        return id;
    }

    @Override
    public void updateUser(@NotNull UUID id, @Valid @NotNull UpdateUserDto updateUserDto) {
        log.debug("updateUser {}, {}", id, updateUserDto);
        User user = userRepository.findById(id).orElseThrow(() -> {
                    IllegalArgumentException ex = new IllegalArgumentException("User with id '%s' not found".formatted(id));
                    log.debug("updateUser", ex);
                    return ex;
        }
        );
        String email = updateUserDto.getEmail();
        if (email != null) {
            userRepository.findByEmail(email).ifPresent(existingUser -> {
                IllegalArgumentException ex = new IllegalArgumentException("User with email %s already exist".formatted(existingUser.getEmail()));
                log.debug("updateUser", ex);
                throw ex;
            });
            user.setEmail(email);
            log.debug("updateUser user {} email = {}", id, email);
        }
        String firstName = updateUserDto.getFirstName();
        if (firstName != null) {
            user.setFirstName(firstName);
            log.debug("updateUser user {} firstName = {}", id, firstName);
        }
        String lastName = updateUserDto.getLastName();
        if (lastName != null) {
            user.setLastName(lastName);
            log.debug("updateUser user {} lastName = {}", id, lastName);
        }
        LocalDate birthDate = updateUserDto.getBirthDate();
        if (birthDate != null) {
            user.setBirthDate(birthDate);
            log.debug("updateUser user {} birthDate = {}", id, birthDate);
        }
        String address = updateUserDto.getAddress();
        if (address != null) {
            user.setAddress(address);
            log.debug("updateUser user {} address = {}", id, address);
        }
        String phoneNumber = updateUserDto.getPhoneNumber();
        if (phoneNumber != null) {
            user.setPhoneNumber(phoneNumber);
            log.debug("updateUser user {} phoneNumber = {}", id, phoneNumber);
        }
        log.debug("end updateUser");
    }

    @Override
    public void deleteUser(@NotNull UUID id) {
        log.debug("deleteUser {}", id);
        userRepository.deleteById(id);
        log.debug("deleteUser deleted {}", id);
        log.debug("end deleteUser");
    }

    @Override
    public List<GetUserDto> findAllByBirthDateRange(@NotNull LocalDate from, @NotNull LocalDate to,
                                                    @NotNull Integer pageIndex, @NotNull @Max(500) Integer pageSize) {
        log.debug("findAllByBirthDateRange {}, {}, {}, {}", from, to, pageIndex, pageIndex);
        if (from.isAfter(to)) {
            IllegalArgumentException ex = new IllegalArgumentException("From date is after to date");
            log.debug("findAllByBirthDateRange", ex);
            throw ex;
        }
        List<GetUserDto> list = userRepository.getAllByBirthDateRange(from, to, PageRequest.of(pageIndex, pageSize)).stream()
                .map(user -> modelMapper.map(user, GetUserDto.class))
                .toList();
        log.debug("end findAllByBirthDateRange {}", list);
        return list;
    }
}
