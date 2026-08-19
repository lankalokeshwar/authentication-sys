package com.example.Authentication_sys.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Travels through SNS -> SQS when a profile is updated.
 * Contains all fields that can change via the /auth/update endpoint.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserUpdatedEvent {

    private String email;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String dateOfBirth;
    private String gender;

    public UserUpdatedEvent() {}

    public UserUpdatedEvent(String email, String firstName, String lastName,
                            String phoneNumber, String dateOfBirth, String gender) {
        this.email       = email;
        this.firstName   = firstName;
        this.lastName    = lastName;
        this.phoneNumber = phoneNumber;
        this.dateOfBirth = dateOfBirth;
        this.gender      = gender;
    }

    public String getEmail()           { return email; }
    public void setEmail(String v)     { this.email = v; }

    public String getFirstName()           { return firstName; }
    public void setFirstName(String v)     { this.firstName = v; }

    public String getLastName()           { return lastName; }
    public void setLastName(String v)     { this.lastName = v; }

    public String getPhoneNumber()           { return phoneNumber; }
    public void setPhoneNumber(String v)     { this.phoneNumber = v; }

    public String getDateOfBirth()           { return dateOfBirth; }
    public void setDateOfBirth(String v)     { this.dateOfBirth = v; }

    public String getGender()           { return gender; }
    public void setGender(String v)     { this.gender = v; }
}
