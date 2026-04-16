package com.myprojects.lovable_clone.mapper;

import com.myprojects.lovable_clone.dto.auth.AuthResponse;
import com.myprojects.lovable_clone.dto.auth.SignupRequest;
import com.myprojects.lovable_clone.dto.auth.UserProfileResponse;
import com.myprojects.lovable_clone.entity.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {
    User toUserEntity(SignupRequest signupRequest);
    UserProfileResponse toUserProfileResponse(User user);
}
