package com.birthday.birthdayapp.controller;

import com.birthday.birthdayapp.dto.ApiResponse;
import com.birthday.birthdayapp.dto.RegisterRequest;
import com.birthday.birthdayapp.entity.User;
import com.birthday.birthdayapp.repository.UserRepository;
import com.birthday.birthdayapp.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final UserRepository userRepository;

    public UserController(UserService userService,  UserRepository userRepository) {
        this.userService = userService;
        this.userRepository = userRepository;
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse> register(@Valid @RequestBody RegisterRequest request) {
        User registeredUser = userService.registerUser(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new ApiResponse(true, "Registration successful for " + registeredUser.getUsername() + "!"));
    }

    @GetMapping("/count")
    public ResponseEntity<Map<String, Object>> getUserCount() {
        long count = userRepository.count();
        Map<String, Object> response = new HashMap<>();
        response.put("count", count);
        return ResponseEntity.ok(response);
    }
}
