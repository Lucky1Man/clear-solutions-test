package org.example.clearsolutionstest.entity;

import jakarta.annotation.Nullable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.validator.constraints.Length;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.UUID;

@Entity(name = "User")
@Table(
        name = "users",
        uniqueConstraints = @UniqueConstraint(
                name = "users_email_key",
                columnNames = "email"
        )
)
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder(setterPrefix = "with")
@EqualsAndHashCode
public class User {

    @Id
    @GeneratedValue
    @Column(name = "id", nullable = false, columnDefinition = "uuid")
    private UUID id;

    @Column(
            name = "email",
            nullable = false,
            columnDefinition = "varchar(500)"
    )
    @Email
    @NotNull(message = "User must have email")
    private String email;

    @Column(
            name = "first_name",
            nullable = false,
            columnDefinition = "varchar(100)"
    )
    @Length(min = 1, max = 100)
    @NotNull(message = "User must have first name")
    private String firstName;

    @Column(
            name = "last_name",
            nullable = false,
            columnDefinition = "varchar(100)"
    )
    @Length(min = 1, max = 100)
    @NotNull(message = "User must have last name")
    private String lastName;

    @NotNull(message = "User must have birth date")
    @Column(
            name = "birth_date",
            nullable = false
    )
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    @Past
    private LocalDate birthDate;

    @Nullable
    @Column(name = "address", columnDefinition = "varchar(200)")
    @Length(max = 200)
    private String address;

    @Column(name = "phone_number", columnDefinition = "varchar(18)")
    @Nullable
    @Pattern(regexp="\\d{8,18}", message="Phone number must be between 8 and 18 digits")
    private String phoneNumber;

}
