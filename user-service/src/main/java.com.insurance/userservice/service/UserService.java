package userservice.service;


import userservice.model.User;

import userservice.dto.UserDto;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserService {

    UserDto registerUser(UserDto userDto); // Consider a dedicated RegistrationRequest DTO

    Optional<UserDto> findUserById(UUID userId);

    Optional<UserDto> findUserByUsername(String username);

    Optional<UserDto> findUserByEmail(String email);

    List<UserDto> findAllUsers();

    UserDto updateUser(UUID userId, UserDto userDto);

    void deleteUser(UUID userId);

    boolean usernameExists(String username);

    boolean emailExists(String email);

    // Add methods for address management if needed within this service
    // Add methods for role management if needed
}

