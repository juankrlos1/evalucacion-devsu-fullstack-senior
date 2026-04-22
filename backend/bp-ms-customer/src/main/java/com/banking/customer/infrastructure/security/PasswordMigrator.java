package com.banking.customer.infrastructure.security;

import com.banking.customer.infrastructure.persistence.entity.CustomerEntity;
import com.banking.customer.infrastructure.persistence.repository.SpringDataCustomerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Hashes any plain-text passwords found in the database on startup.
 * BCrypt hashes always start with "$2a$", so unhashed passwords are detected easily.
 *
 * @author Juan Carlos De La Cruz Perez - Devsu
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PasswordMigrator implements ApplicationRunner {

    private final SpringDataCustomerRepository repository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        List<CustomerEntity> customers = repository.findAll();
        int migrated = 0;
        for (CustomerEntity entity : customers) {
            if (!entity.getPassword().startsWith("$2a$")) {
                entity.setPassword(passwordEncoder.encode(entity.getPassword()));
                repository.save(entity);
                migrated++;
            }
        }
        if (migrated > 0) {
            log.info("Migrated {} plain-text passwords to BCrypt", migrated);
        }
    }
}
