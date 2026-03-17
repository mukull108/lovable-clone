package com.myprojects.lovable_clone.service;

import com.myprojects.lovable_clone.dto.auth.UserProfileResponse;

public interface UserService {
    UserProfileResponse getProfile(Long userId);
}
