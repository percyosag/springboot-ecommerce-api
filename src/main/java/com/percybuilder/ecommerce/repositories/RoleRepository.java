package com.percybuilder.ecommerce.repositories;

import com.percybuilder.ecommerce.models.Role;
import com.percybuilder.ecommerce.models.RoleName;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {

    Optional<Role> findByName(RoleName name);

    boolean existsByName(RoleName name);
}