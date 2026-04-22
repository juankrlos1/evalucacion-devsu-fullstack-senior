package com.banking.customer.infrastructure.persistence.adapter;

import com.banking.customer.domain.model.Customer;
import com.banking.customer.domain.port.out.CustomerRepository;
import com.banking.customer.infrastructure.persistence.mapper.CustomerEntityMapper;
import com.banking.customer.infrastructure.persistence.repository.SpringDataCustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class CustomerPersistenceAdapter implements CustomerRepository {

    private final SpringDataCustomerRepository jpaRepository;
    private final CustomerEntityMapper mapper;

    @Override
    public Customer save(Customer customer) {
        var entity = mapper.toEntity(customer);
        var saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<Customer> findByCustomerId(String customerId) {
        return jpaRepository.findById(customerId).map(mapper::toDomain);
    }

    @Override
    public Optional<Customer> findByIdentification(String identification) {
        return jpaRepository.findByIdentification(identification).map(mapper::toDomain);
    }

    @Override
    public List<Customer> findAll() {
        return jpaRepository.findAll().stream().map(mapper::toDomain).toList();
    }

    @Override
    public boolean existsByCustomerId(String customerId) {
        return jpaRepository.existsById(customerId);
    }
}
