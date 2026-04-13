package com.myprojects.lovable_clone.service.impl;

import com.myprojects.lovable_clone.dto.auth.AuthResponse;
import com.myprojects.lovable_clone.dto.auth.LoginRequest;
import com.myprojects.lovable_clone.dto.auth.SignupRequest;
import com.myprojects.lovable_clone.service.AuthService;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {
    @Override
    public AuthResponse signup(SignupRequest signupRequest) {
        return null;
    }

    @Override
    public AuthResponse login(LoginRequest loginRequest) {
        return null;
    }
}
