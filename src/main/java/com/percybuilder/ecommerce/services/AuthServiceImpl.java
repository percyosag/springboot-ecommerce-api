package com.percybuilder.ecommerce.services;

import com.percybuilder.ecommerce.dtos.AuthResponse;
import com.percybuilder.ecommerce.dtos.LoginRequest;
import com.percybuilder.ecommerce.dtos.RegisterRequest;
import com.percybuilder.ecommerce.dtos.UserProfileResponse;
import com.percybuilder.ecommerce.exceptions.BadRequestException;
import com.percybuilder.ecommerce.exceptions.ResourceNotFoundException;
import com.percybuilder.ecommerce.models.AppUser;
import com.percybuilder.ecommerce.models.Role;
import com.percybuilder.ecommerce.models.RoleName;
import com.percybuilder.ecommerce.repositories.AppUserRepository;
import com.percybuilder.ecommerce.repositories.RoleRepository;
import com.percybuilder.ecommerce.security.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AuthServiceImpl implements AuthService {

    private final AppUserRepository appUserRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthServiceImpl(
            AppUserRepository appUserRepository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            JwtService jwtService
    ) {
        this.appUserRepository = appUserRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    @Override
    public AuthResponse register(RegisterRequest registerRequest) {
        if (appUserRepository.existsByEmail(registerRequest.getEmail())) {
            throw new BadRequestException("Email is already registered");
        }

        if (appUserRepository.existsByUsername(registerRequest.getUsername())) {
            throw new BadRequestException("Username is already taken");
        }

        Role userRole = roleRepository.findByName(RoleName.ROLE_USER)
                .orElseGet(() -> roleRepository.save(
                        Role.builder()
                                .name(RoleName.ROLE_USER)
                                .build()
                ));

        AppUser appUser = AppUser.builder()
                .firstName(registerRequest.getFirstName())
                .lastName(registerRequest.getLastName())
                .email(registerRequest.getEmail())
                .username(registerRequest.getUsername())
                .passwordHash(passwordEncoder.encode(registerRequest.getPassword()))
                .roles(Set.of(userRole))
                .build();

        AppUser savedUser = appUserRepository.save(appUser);

        return buildAuthResponse(savedUser);
    }

    @Override
    public AuthResponse login(LoginRequest loginRequest) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsernameOrEmail(),
                        loginRequest.getPassword()
                )
        );

        AppUser appUser = appUserRepository.findByUsername(loginRequest.getUsernameOrEmail())
                .or(() -> appUserRepository.findByEmail(loginRequest.getUsernameOrEmail()))
                .orElseThrow(() -> new BadRequestException("Invalid username/email or password"));

        return buildAuthResponse(appUser);
    }

    @Override
    public UserProfileResponse getCurrentUser(String username) {
        AppUser appUser = appUserRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found with username: " + username
                ));

        return UserProfileResponse.builder()
                .id(appUser.getId())
                .firstName(appUser.getFirstName())
                .lastName(appUser.getLastName())
                .username(appUser.getUsername())
                .email(appUser.getEmail())
                .roles(extractRoles(appUser))
                .build();
    }

    private AuthResponse buildAuthResponse(AppUser appUser) {
        Set<String> roles = extractRoles(appUser);

        String token = jwtService.generateToken(appUser.getUsername(), roles);

        return AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .userId(appUser.getId())
                .username(appUser.getUsername())
                .email(appUser.getEmail())
                .roles(roles)
                .build();
    }

    private Set<String> extractRoles(AppUser appUser) {
        return appUser.getRoles()
                .stream()
                .map(role -> role.getName().name())
                .collect(Collectors.toSet());
    }
}