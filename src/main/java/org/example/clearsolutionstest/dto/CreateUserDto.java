package org.example.clearsolutionstest.dto;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Generated;
import lombok.NoArgsConstructor;
import org.example.clearsolutionstest.validator.EighteenPlusConstraint;
import org.hibernate.validator.constraints.Length;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Generated
public class CreateUserDto {

    @Email
    @NotNull(message = "User must have email")
    private String email;

    @Length(min = 1, max = 100)
    @NotNull(message = "User must have full name")
    private String firstName;

    @Length(min = 1, max = 100)
    @NotNull(message = "User must have full name")
    private String lastName;

    @NotNull(message = "User must have birth date")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    @EighteenPlusConstraint
    private LocalDate birthDate;

    @Nullable
    @Length(max = 200)
    private String address;

    @Nullable
    @Pattern(regexp="\\d{8,18}", message="Phone number must be between 8 and 18 digits and contain only digits")
    private String phoneNumber;
}
