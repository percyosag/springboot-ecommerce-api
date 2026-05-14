package com.percybuilder.ecommerce.services;

import com.percybuilder.ecommerce.dtos.AuthResponse;
import com.percybuilder.ecommerce.dtos.LoginRequest;
import com.percybuilder.ecommerce.dtos.RegisterRequest;
import com.percybuilder.ecommerce.exceptions.BadRequestException;
import com.percybuilder.ecommerce.models.AppUser;
import com.percybuilder.ecommerce.models.Role;
import com.percybuilder.ecommerce.models.RoleName;
import com.percybuilder.ecommerce.repositories.AppUserRepository;
import com.percybuilder.ecommerce.repositories.RoleRepository;
import com.percybuilder.ecommerce.security.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private AppUserRepository appUserRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthServiceImpl authService;

    @Test
    void register_shouldCreateUserAndReturnAuthResponse_whenEmailAndUsernameAreAvailable() {
        RegisterRequest request = buildRegisterRequest();
        Role userRole = buildUserRole();
        AppUser savedUser = buildUser(userRole);

        when(appUserRepository.existsByEmail("percy@example.com")).thenReturn(false);
        when(appUserRepository.existsByUsername("percy")).thenReturn(false);
        when(roleRepository.findByName(RoleName.ROLE_USER)).thenReturn(Optional.of(userRole));
        when(passwordEncoder.encode("Password123")).thenReturn("hashed-password");
        when(appUserRepository.save(any(AppUser.class))).thenReturn(savedUser);
        when(jwtService.generateToken(eq("percy"), anySet())).thenReturn("jwt-token");

        AuthResponse response = authService.register(request);

        assertThat(response.getToken()).isEqualTo("jwt-token");
        assertThat(response.getTokenType()).isEqualTo("Bearer");
        assertThat(response.getUserId()).isEqualTo(1L);
        assertThat(response.getUsername()).isEqualTo("percy");
        assertThat(response.getEmail()).isEqualTo("percy@example.com");
        assertThat(response.getRoles()).containsExactly("ROLE_USER");

        verify(appUserRepository).existsByEmail("percy@example.com");
        verify(appUserRepository).existsByUsername("percy");
        verify(passwordEncoder).encode("Password123");
        verify(appUserRepository).save(any(AppUser.class));
        verify(jwtService).generateToken(eq("percy"), anySet());
    }

    @Test
    void register_shouldThrowBadRequest_whenEmailAlreadyExists() {
        RegisterRequest request = buildRegisterRequest();

        when(appUserRepository.existsByEmail("percy@example.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Email is already registered");

        verify(appUserRepository).existsByEmail("percy@example.com");
        verify(appUserRepository, never()).save(any(AppUser.class));
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    void register_shouldThrowBadRequest_whenUsernameAlreadyExists() {
        RegisterRequest request = buildRegisterRequest();

        when(appUserRepository.existsByEmail("percy@example.com")).thenReturn(false);
        when(appUserRepository.existsByUsername("percy")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Username is already taken");

        verify(appUserRepository).existsByEmail("percy@example.com");
        verify(appUserRepository).existsByUsername("percy");
        verify(appUserRepository, never()).save(any(AppUser.class));
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    void login_shouldAuthenticateUserAndReturnAuthResponse_whenCredentialsAreValid() {
        LoginRequest request = new LoginRequest();
        request.setUsernameOrEmail("percy");
        request.setPassword("Password123");

        Role userRole = buildUserRole();
        AppUser appUser = buildUser(userRole);

        when(appUserRepository.findByUsername("percy")).thenReturn(Optional.of(appUser));
        when(jwtService.generateToken(eq("percy"), anySet())).thenReturn("jwt-token");

        AuthResponse response = authService.login(request);

        assertThat(response.getToken()).isEqualTo("jwt-token");
        assertThat(response.getUsername()).isEqualTo("percy");
        assertThat(response.getEmail()).isEqualTo("percy@example.com");
        assertThat(response.getRoles()).containsExactly("ROLE_USER");

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(appUserRepository).findByUsername("percy");
        verify(appUserRepository, never()).findByEmail(anyString());
        verify(jwtService).generateToken(eq("percy"), anySet());
    }

    private RegisterRequest buildRegisterRequest() {
        RegisterRequest request = new RegisterRequest();
        request.setFirstName("Percy");
        request.setLastName("Osunde");
        request.setEmail("percy@example.com");
        request.setUsername("percy");
        request.setPassword("Password123");
        return request;
    }

    private Role buildUserRole() {
        return Role.builder()
                .id(1L)
                .name(RoleName.ROLE_USER)
                .build();
    }

    private AppUser buildUser(Role userRole) {
        return AppUser.builder()
                .id(1L)
                .firstName("Percy")
                .lastName("Osunde")
                .email("percy@example.com")
                .username("percy")
                .passwordHash("hashed-password")
                .roles(Set.of(userRole))
                .build();
    }
}