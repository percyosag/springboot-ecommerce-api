package com.percybuilder.ecommerce.repositories;

import com.percybuilder.ecommerce.models.Address;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AddressRepository extends JpaRepository<Address, Long> {

    List<Address> findByAppUserUsername(String username);

    Optional<Address> findByAppUserUsernameAndId(String username, Long id);

    boolean existsByAppUserUsername(String username);
}