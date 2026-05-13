package com.percybuilder.ecommerce.repositories;

import com.percybuilder.ecommerce.models.CustomerOrder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<CustomerOrder, Long> {

    List<CustomerOrder> findByAppUserUsernameOrderByCreatedAtDesc(String username);

    Optional<CustomerOrder> findByIdAndAppUserUsername(Long id, String username);
}