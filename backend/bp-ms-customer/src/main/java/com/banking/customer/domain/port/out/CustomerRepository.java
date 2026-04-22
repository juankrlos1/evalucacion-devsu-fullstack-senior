package com.banking.customer.domain.port.out;

import com.banking.customer.domain.model.Customer;

import java.util.List;
import java.util.Optional;

/**
 * Output port for customer persistence operations.
 */
public interface CustomerRepository {

    Customer save(Customer customer);

    Optional<Customer> findByCustomerId(String customerId);

    Optional<Customer> findByIdentification(String identification);

    List<Customer> findAll();

    boolean existsByCustomerId(String customerId);
}
