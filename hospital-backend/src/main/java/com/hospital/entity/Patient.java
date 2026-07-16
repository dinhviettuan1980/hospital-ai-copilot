package com.hospital.entity;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "patient")
public class Patient extends BaseEntity {

    @NotBlank
    @Size(max = 80)
    @Column(name = "first_name", nullable = false, length = 80)
    public String firstName;

    @NotBlank
    @Size(max = 80)
    @Column(name = "last_name", nullable = false, length = 80)
    public String lastName;

    @NotNull
    @Past
    @Column(name = "date_of_birth", nullable = false)
    public LocalDate dateOfBirth;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "gender", nullable = false, length = 10)
    public Gender gender;

    @Size(max = 30)
    @Column(name = "phone", length = 30)
    public String phone;

    @Email
    @Size(max = 150)
    @Column(name = "email", length = 150)
    public String email;

    @Size(max = 250)
    @Column(name = "address", length = 250)
    public String address;

    public String fullName() {
        return firstName + " " + lastName;
    }
}
