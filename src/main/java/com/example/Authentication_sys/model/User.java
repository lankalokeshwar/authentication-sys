package com.example.Authentication_sys.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String firstName;
    private String lastName;

    @Column(unique = true, nullable = false)
    private String email;

    private String phoneNumber;

    @JsonIgnore
    private String password;

    private String dateOfBirth;
    private String gender;
    private String role;

    public String getFullName() {
        return firstName + " " + lastName;
    }
}
