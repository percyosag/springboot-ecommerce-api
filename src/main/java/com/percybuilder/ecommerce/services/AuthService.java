package com.percybuilder.ecommerce.services;

import com.percybuilder.ecommerce.dtos.AuthResponse;
import com.percybuilder.ecommerce.dtos.LoginRequest;
import com.percybuilder.ecommerce.dtos.RegisterRequest;
import com.percybuilder.ecommerce.dtos.UserProfileResponse;

public interface AuthService {

    AuthResponse register(RegisterRequest registerRequest);

    AuthResponse login(LoginRequest loginRequest);

    UserProfileResponse getCurrentUser(String username);
}