package com.percybuilder.ecommerce.config;

import com.percybuilder.ecommerce.models.AppUser;
import com.percybuilder.ecommerce.models.Role;
import com.percybuilder.ecommerce.models.RoleName;
import com.percybuilder.ecommerce.repositories.AppUserRepository;
import com.percybuilder.ecommerce.repositories.RoleRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.seed-enabled:false}")
    private boolean adminSeedEnabled;

    @Value("${app.admin.username:admin}")
    private String adminUsername;

    @Value("${app.admin.email:admin@example.com}")
    private String adminEmail;

    @Value("${app.admin.password:}")
    private String adminPassword;

    public DataInitializer(
            RoleRepository roleRepository,
            AppUserRepository appUserRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.roleRepository = roleRepository;
        this.appUserRepository = appUserRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        Role userRole = roleRepository.findByName(RoleName.ROLE_USER)
                .orElseGet(() -> roleRepository.save(
                        Role.builder()
                                .name(RoleName.ROLE_USER)
                                .build()
                ));

        Role adminRole = roleRepository.findByName(RoleName.ROLE_ADMIN)
                .orElseGet(() -> roleRepository.save(
                        Role.builder()
                                .name(RoleName.ROLE_ADMIN)
                                .build()
                ));

        if (!adminSeedEnabled || adminPassword == null || adminPassword.isBlank()) {
            return;
        }

        AppUser existingAdmin = appUserRepository.findByUsername(adminUsername)
                .or(() -> appUserRepository.findByEmail(adminEmail))
                .orElse(null);

        if (existingAdmin != null) {
            existingAdmin.getRoles().add(userRole);
            existingAdmin.getRoles().add(adminRole);
            appUserRepository.save(existingAdmin);
            return;
        }

        Set<Role> adminRoles = new HashSet<>();
        adminRoles.add(userRole);
        adminRoles.add(adminRole);

        AppUser adminUser = AppUser.builder()
                .firstName("Admin")
                .lastName("User")
                .email(adminEmail)
                .username(adminUsername)
                .passwordHash(passwordEncoder.encode(adminPassword))
                .roles(adminRoles)
                .build();

        appUserRepository.save(adminUser);
    }
}