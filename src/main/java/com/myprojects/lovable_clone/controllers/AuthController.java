package com.myprojects.lovable_clone.controllers;

import com.myprojects.lovable_clone.dto.auth.AuthResponse;
import com.myprojects.lovable_clone.dto.auth.LoginRequest;
import com.myprojects.lovable_clone.dto.auth.SignupRequest;
import com.myprojects.lovable_clone.dto.auth.UserProfileResponse;
import com.myprojects.lovable_clone.security.JwtUserPrincipal;
import com.myprojects.lovable_clone.service.AuthService;
import com.myprojects.lovable_clone.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/api/auth")
@FieldDefaults(makeFinal = true, level = lombok.AccessLevel.PRIVATE)
public class AuthController {

    AuthService authService;
    UserService userService;

    @PostMapping("/signup")
    public ResponseEntity<AuthResponse> signUp(@RequestBody SignupRequest signupRequest){
        return ResponseEntity.ok(authService.signup(signupRequest));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest loginRequest){
        return ResponseEntity.ok(authService.login(loginRequest));
    }

    @GetMapping("/me")
    public ResponseEntity<UserProfileResponse> getProfile(){
        JwtUserPrincipal principal = (JwtUserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return ResponseEntity.ok(userService.getProfile(principal.getUserId()));

    }

}
