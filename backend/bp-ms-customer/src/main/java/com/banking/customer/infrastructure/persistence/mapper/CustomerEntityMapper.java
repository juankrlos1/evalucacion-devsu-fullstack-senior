package com.banking.customer.infrastructure.persistence.mapper;

import com.banking.customer.domain.model.Customer;
import com.banking.customer.domain.model.PersonData;
import com.banking.customer.infrastructure.persistence.entity.CustomerEntity;
import org.springframework.stereotype.Component;

@Component
public class CustomerEntityMapper {

    public Customer toDomain(CustomerEntity entity) {
        var data = new PersonData(
                entity.getName(), entity.getGender(), entity.getAge(),
                entity.getIdentification(), entity.getAddress(), entity.getPhone()
        );
        return new Customer(entity.getCustomerId(), entity.getPassword(), entity.getRole(), entity.isActive(), data);
    }

    public CustomerEntity toEntity(Customer domain) {
        var entity = new CustomerEntity();
        entity.setCustomerId(domain.getCustomerId());
        entity.setPassword(domain.getPassword());
        entity.setRole(domain.getRole());
        entity.setActive(domain.isActive());
        entity.setName(domain.getName());
        entity.setGender(domain.getGender());
        entity.setAge(domain.getAge());
        entity.setIdentification(domain.getIdentification());
        entity.setAddress(domain.getAddress());
        entity.setPhone(domain.getPhone());
        return entity;
    }
}
