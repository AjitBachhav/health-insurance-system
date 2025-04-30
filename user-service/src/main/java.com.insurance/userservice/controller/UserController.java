package userservice.controller;

import userservice.dto.UserDto;
import userservice.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users") // Base path for user endpoints
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    // Endpoint for user registration (Placeholder - needs proper DTO and security)
    @PostMapping("/register")
    public ResponseEntity<UserDto> registerUser(@Valid @RequestBody UserDto userDto) {
        // TODO: Use a dedicated RegistrationRequest DTO with password
        // TODO: Add validation
        UserDto registeredUser = userService.registerUser(userDto);
        return new ResponseEntity<>(registeredUser, HttpStatus.CREATED);
    }

    // Endpoint to get all users (Consider pagination and security)
    @GetMapping
    public ResponseEntity<List<UserDto>> getAllUsers() {
        // TODO: Add security check (e.g., only ADMIN role)
        List<UserDto> users = userService.findAllUsers();
        return ResponseEntity.ok(users);
    }

    // Endpoint to get a user by ID
    @GetMapping("/{userId}")
    public ResponseEntity<UserDto> getUserById(@PathVariable UUID userId) {
        // TODO: Add security check (e.g., user can get own profile, admin can get any)
        return userService.findUserById(userId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Endpoint to get a user by username
    @GetMapping("/username/{username}")
    public ResponseEntity<UserDto> getUserByUsername(@PathVariable String username) {
        // TODO: Add security check
        return userService.findUserByUsername(username)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Endpoint to update a user (Placeholder - needs proper DTO and security)
    @PutMapping("/{userId}")
    public ResponseEntity<UserDto> updateUser(@PathVariable UUID userId, @Valid @RequestBody UserDto userDto) {
        // TODO: Use a dedicated UpdateUserRequest DTO
        // TODO: Add security check (user can update own profile, admin can update any)
        try {
            UserDto updatedUser = userService.updateUser(userId, userDto);
            return ResponseEntity.ok(updatedUser);
        } catch (jakarta.persistence.EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
        // Add handling for other potential exceptions
    }

    // Endpoint to delete a user
    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable UUID userId) {
        // TODO: Add security check (e.g., only ADMIN role)
        try {
            userService.deleteUser(userId);
            return ResponseEntity.noContent().build();
        } catch (jakarta.persistence.EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // TODO: Add endpoints for managing addresses if required
    // TODO: Add endpoints related to authentication/login (likely in a separate AuthController)
}

