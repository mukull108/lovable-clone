package com.myprojects.lovable_clone.service;

import com.myprojects.lovable_clone.dto.auth.AuthResponse;
import com.myprojects.lovable_clone.dto.auth.LoginRequest;
import com.myprojects.lovable_clone.dto.auth.SignupRequest;

public interface AuthService {
    AuthResponse signup(SignupRequest signupRequest);

    AuthResponse login(LoginRequest loginRequest);
}
