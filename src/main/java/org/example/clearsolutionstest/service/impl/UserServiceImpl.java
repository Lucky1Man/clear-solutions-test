package org.example.clearsolutionstest.service.impl;

import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.example.clearsolutionstest.dto.CreateUserDto;
import org.example.clearsolutionstest.dto.GetUserDto;
import org.example.clearsolutionstest.dto.UpdateUserDto;
import org.example.clearsolutionstest.entity.User;
import org.example.clearsolutionstest.repository.UserRepository;
import org.example.clearsolutionstest.service.UserService;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    private final ModelMapper modelMapper;

    @Override
    public UUID createUser(@Valid @NotNull CreateUserDto createUserDto) {
        return userRepository.save(modelMapper.map(createUserDto, User.class)).getId();
    }

    @Override
    public void updateUser(@NotNull UUID id, @Valid @NotNull UpdateUserDto updateUserDto) {
        User user = userRepository.findById(id).orElseThrow(() ->
                new IllegalArgumentException("User with id '%s' not found".formatted(id))
        );
        String email = updateUserDto.getEmail();
        if (email != null) {
            userRepository.findByEmail(email).ifPresent(existingUser -> {
                throw new IllegalArgumentException("User with email %s already exist".formatted(existingUser.getEmail()));
            });
            user.setEmail(email);
        }
        String firstName = updateUserDto.getFirstName();
        if (firstName != null) {
            user.setFirstName(firstName);
        }
        String lastName = updateUserDto.getLastName();
        if (lastName != null) {
            user.setLastName(lastName);
        }
        LocalDate birthDate = updateUserDto.getBirthDate();
        if (birthDate != null) {
            user.setBirthDate(birthDate);
        }
        String address = updateUserDto.getAddress();
        if (address != null) {
            user.setAddress(address);
        }
        String phoneNumber = updateUserDto.getPhoneNumber();
        if (phoneNumber != null) {
            user.setPhoneNumber(phoneNumber);
        }
    }

    @Override
    public void deleteUser(@NotNull UUID id) {
        userRepository.deleteById(id);
    }

    @Override
    public List<GetUserDto> findAllByBirthDateRange(@NotNull LocalDate from, @NotNull LocalDate to) {
        if (from.isAfter(to)) {
            throw new IllegalArgumentException("From date is after to date");
        }
        return userRepository.getAllByBirthDateRange(from, to).stream()
                .map(user -> modelMapper.map(user, GetUserDto.class))
                .toList();
    }
}
