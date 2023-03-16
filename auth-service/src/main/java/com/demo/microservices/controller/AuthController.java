package com.demo.microservices.controller;

import com.demo.microservices.dto.LoginRequest;
import com.demo.microservices.dto.SignupRequest;
import com.demo.microservices.model.ERole;
import com.demo.microservices.model.Role;
import com.demo.microservices.model.User;
import com.demo.microservices.repository.RoleRepository;
import com.demo.microservices.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.Set;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoleRepository roleRepository;

    @PostMapping("/signin")
    public ResponseEntity<?> signIn(@RequestBody LoginRequest loginRequest) {
        var user = userRepository.findByEmail(loginRequest.getMail());
        if(user == null || !user.getPassword().equals(loginRequest.getPassword())){
            return new ResponseEntity<>("Try again. Provided password or mail is not correct.", HttpStatus.BAD_REQUEST);
        }

        // return JWT token
        return ResponseEntity.ok("successfully logged in");
    }

    @PostMapping("/signup")
    public ResponseEntity<?> signUp(@RequestBody SignupRequest signUpRequest) {
        if (userRepository.existsByEmail(signUpRequest.getMail())) {
            return new ResponseEntity<>("Error: Email is already in use!", HttpStatus.BAD_REQUEST);
        }

        createNewUser(signUpRequest);
        return new ResponseEntity<>("User registered successfully!", HttpStatus.OK);
    }

    @GetMapping("/token/validate")
    public ResponseEntity<?> validate(@RequestBody String token) {
        return new ResponseEntity<>("All good!", HttpStatus.OK);
    }

    private User createNewUser(SignupRequest signUpRequest) {
        // Create new user's account
        User user = new User(signUpRequest.getFullName(),
                signUpRequest.getMail(),
                signUpRequest.getPassword());

        Set<Role> roles = new HashSet<>();


        Role userRole = roleRepository.findByName(ERole.ROLE_CLIENT)
                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
        roles.add(userRole);

        user.setRoles(roles);
        userRepository.save(user);
        return user;
    }
}
