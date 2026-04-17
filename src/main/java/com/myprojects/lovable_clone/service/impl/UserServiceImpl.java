package com.myprojects.lovable_clone.service.impl;

import com.myprojects.lovable_clone.dto.auth.UserProfileResponse;
import com.myprojects.lovable_clone.entity.User;
import com.myprojects.lovable_clone.exceptions.ResourceNotFoundException;
import com.myprojects.lovable_clone.repository.UserRepository;
import com.myprojects.lovable_clone.security.AuthUtils;
import com.myprojects.lovable_clone.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = lombok.AccessLevel.PRIVATE)
public class UserServiceImpl implements UserService, UserDetailsService {
    UserRepository userRepository;
    AuthUtils authUtils;

    @Override
    public UserProfileResponse getProfile() {
        Long userId = authUtils.getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId.toString()));
        return new UserProfileResponse(user.getId(), user.getName(), user.getUsername());
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findUserByUsername(username).orElseThrow(
                () -> new ResourceNotFoundException("User", username));
    }
}
