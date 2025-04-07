package group.quankane.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import group.quankane.util.*;
import jakarta.validation.constraints.*;
import org.apache.logging.log4j.message.Message;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.text.Format;
import java.util.Date;
import java.util.List;

import static group.quankane.util.Gender.*;

public class UserRequestDTO implements Serializable {
    @NotBlank(message = "firstName must be not blank")
    private String firstName;
    @NotNull(message = "lastName must be not null")
    private String lastName;
    @Email(message = "email invalid format")
    private String email;
    @PhoneNumber
    private String phone;
    @NotNull(message = "dateOfBirth must be not null")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    @JsonFormat(pattern = "MM/dd/yyyy")
    private Date dateOfBirth;
    @GenderSubset(anyOf = {MALE, FEMALE, OTHER})
    private Gender gender;
    @NotNull(message = "username must be not null")
    private String username;
    @NotNull(message = "password must be not null")
    private String password;
    @NotNull(message = "type must be not null")
    @EnumValue(name = "type", enumClass = UserType.class)
    private String type;
//    @NotNull(message = "List not be null")
//    private List<Object> addresses;
    @EnumPattern(name = "status", regexp = "ACTIVE|INACTIVE|NONE")
    private UserStatus status;
    public UserRequestDTO(String firstName, String lastName, String email, String phone) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.phone = phone;
        this.email = email;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Date getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(Date dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public UserStatus getStatus() {
        return status;
    }

    public void setStatus(UserStatus status) {
        this.status = status;
    }

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
