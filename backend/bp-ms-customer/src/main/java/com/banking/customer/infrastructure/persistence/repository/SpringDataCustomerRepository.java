package com.banking.customer.infrastructure.persistence.repository;

import com.banking.customer.infrastructure.persistence.entity.CustomerEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SpringDataCustomerRepository extends JpaRepository<CustomerEntity, String> {

    Optional<CustomerEntity> findByIdentification(String identification);
}
