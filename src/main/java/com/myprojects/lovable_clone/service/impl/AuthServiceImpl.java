package com.myprojects.lovable_clone.service.impl;

import com.myprojects.lovable_clone.dto.auth.AuthResponse;
import com.myprojects.lovable_clone.dto.auth.LoginRequest;
import com.myprojects.lovable_clone.dto.auth.SignupRequest;
import com.myprojects.lovable_clone.entity.User;
import com.myprojects.lovable_clone.exceptions.BadRequestException;
import com.myprojects.lovable_clone.mapper.UserMapper;
import com.myprojects.lovable_clone.repository.UserRepository;
import com.myprojects.lovable_clone.security.AuthUtils;
import com.myprojects.lovable_clone.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@FieldDefaults(makeFinal = true, level = lombok.AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    UserRepository userRepository;
    UserMapper userMapper;
    PasswordEncoder passwordEncoder;
    AuthUtils authUtils;
    AuthenticationManager authenticationManager;

    @Override
    public AuthResponse signup(SignupRequest signupRequest) {
        userRepository.findUserByUsername(signupRequest.username()).ifPresent(user -> {
            throw new BadRequestException("Username is already taken with username: " + signupRequest.username());
        });
        //else create user and save it
        User user = userMapper.toUserEntity(signupRequest);
        user.setPassword(passwordEncoder.encode(signupRequest.password()));
        user = userRepository.save(user);
        String token = authUtils.generateAccessToken(user);
        return new AuthResponse(token, userMapper.toUserProfileResponse(user));
    }

    @Override
    public AuthResponse login(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.username(), loginRequest.password())
        );

        User user = (User) authentication.getPrincipal();
        String token = authUtils.generateAccessToken(user);
        return new AuthResponse(token, userMapper.toUserProfileResponse(user));
    }
}
