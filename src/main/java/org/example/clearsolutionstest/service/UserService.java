package org.example.clearsolutionstest.service;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import org.example.clearsolutionstest.dto.CreateUserDto;
import org.example.clearsolutionstest.dto.GetUserDto;
import org.example.clearsolutionstest.dto.UpdateUserDto;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Validated
public interface UserService {

    UUID createUser(@Valid @NotNull CreateUserDto createUserDto);

    void updateUser(@NotNull UUID id, @Valid @NotNull UpdateUserDto updateUserDto);

    void deleteUser(@NotNull UUID id);

    List<GetUserDto> findAllByBirthDateRange(@NotNull LocalDate from, @NotNull LocalDate to,
                                             @NotNull Integer pageIndex, @NotNull @Max(500) Integer pageSize);

}
