package com.banking.customer.application.usecase;

import com.banking.customer.application.dto.CustomerPatchRequest;
import com.banking.customer.application.dto.CustomerRequest;
import com.banking.customer.application.dto.CustomerResponse;
import com.banking.customer.application.mapper.CustomerMapper;
import com.banking.customer.domain.exception.CustomerNotFoundException;
import com.banking.customer.domain.exception.DuplicateCustomerException;
import com.banking.customer.domain.model.Customer;
import com.banking.customer.domain.model.PersonData;
import com.banking.customer.domain.port.out.CustomerEventPublisher;
import com.banking.customer.domain.port.out.CustomerRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerUseCaseTest {

    @Mock
    private CustomerRepository repository;

    @Mock
    private CustomerMapper mapper;

    @Mock
    private CustomerEventPublisher eventPublisher;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private CustomerUseCase customerUseCase;

    private Customer createCustomer(String id, boolean active) {
        var data = new PersonData("Jose Lema", "Male", 32, "0102030405", "Otavalo sn y principal", "098254785");
        return new Customer(id, "hashed", "USER", active, data);
    }

    @Test
    @DisplayName("Should create customer successfully")
    void shouldCreateCustomer() {
        var request = new CustomerRequest("Jose Lema", "Male", 32,
                "0102030405", "Otavalo sn y principal", "098254785", "1234", true);
        var customer = createCustomer("CLI-001", true);
        var response = new CustomerResponse("CLI-001", "Jose Lema", "Male", 32,
                "0102030405", "Otavalo sn y principal", "098254785", "USER", true);

        when(repository.findByIdentification("0102030405")).thenReturn(Optional.empty());
        when(repository.existsByCustomerId(anyString())).thenReturn(false);
        when(mapper.toDomain(anyString(), eq(request))).thenReturn(customer);
        when(passwordEncoder.encode(anyString())).thenReturn("hashed");
        when(repository.save(any(Customer.class))).thenReturn(customer);
        when(mapper.toResponse(customer)).thenReturn(response);

        StepVerifier.create(customerUseCase.create(request))
                .assertNext(r -> {
                    assertThat(r.customerId()).isEqualTo("CLI-001");
                    assertThat(r.name()).isEqualTo("Jose Lema");
                })
                .verifyComplete();

        verify(eventPublisher).publish(any());
    }

    @Test
    @DisplayName("Should reject duplicate identification")
    void shouldRejectDuplicateIdentification() {
        var request = new CustomerRequest("Jose Lema", "Male", 32,
                "0102030405", "Otavalo sn y principal", "098254785", "1234", true);
        var existing = createCustomer("CLI-001", true);

        when(repository.findByIdentification("0102030405")).thenReturn(Optional.of(existing));

        StepVerifier.create(customerUseCase.create(request))
                .expectError(DuplicateCustomerException.class)
                .verify();
    }

    @Test
    @DisplayName("Should get customer by ID")
    void shouldGetCustomerById() {
        var customer = createCustomer("CLI-001", true);
        var response = new CustomerResponse("CLI-001", "Jose Lema", "Male", 32,
                "0102030405", "Otavalo sn y principal", "098254785", "USER", true);

        when(repository.findByCustomerId("CLI-001")).thenReturn(Optional.of(customer));
        when(mapper.toResponse(customer)).thenReturn(response);

        StepVerifier.create(customerUseCase.get("CLI-001"))
                .assertNext(r -> assertThat(r.customerId()).isEqualTo("CLI-001"))
                .verifyComplete();
    }

    @Test
    @DisplayName("Should throw 404 for non-existent customer")
    void shouldThrow404ForNonExistentCustomer() {
        when(repository.findByCustomerId("NOPE")).thenReturn(Optional.empty());

        StepVerifier.create(customerUseCase.get("NOPE"))
                .expectError(CustomerNotFoundException.class)
                .verify();
    }

    @Test
    @DisplayName("Should list all customers")
    void shouldListCustomers() {
        var c1 = createCustomer("CLI-001", true);
        var c2 = createCustomer("CLI-002", true);
        var r1 = new CustomerResponse("CLI-001", "Jose Lema", "Male", 32,
                "0102030405", "Otavalo sn y principal", "098254785", "USER", true);
        var r2 = new CustomerResponse("CLI-002", "Jose Lema", "Male", 32,
                "0102030405", "Otavalo sn y principal", "098254785", "USER", true);

        when(repository.findAll()).thenReturn(List.of(c1, c2));
        when(mapper.toResponse(c1)).thenReturn(r1);
        when(mapper.toResponse(c2)).thenReturn(r2);

        StepVerifier.create(customerUseCase.list().collectList())
                .assertNext(list -> assertThat(list).hasSize(2))
                .verifyComplete();
    }

    @Test
    @DisplayName("Should deactivate customer on delete")
    void shouldDeactivateOnDelete() {
        var customer = createCustomer("CLI-001", true);
        var deactivated = customer.deactivate();

        when(repository.findByCustomerId("CLI-001")).thenReturn(Optional.of(customer));
        when(repository.save(any(Customer.class))).thenReturn(deactivated);

        StepVerifier.create(customerUseCase.delete("CLI-001"))
                .verifyComplete();

        verify(repository).save(argThat(c -> !c.isActive()));
        verify(eventPublisher).publish(any());
    }

    @Test
    @DisplayName("Should patch customer partially")
    void shouldPatchCustomer() {
        var customer = createCustomer("CLI-001", true);
        var patchRequest = new CustomerPatchRequest("Jose Updated", null, null, null, null, "099999999", null, null);
        var patched = new Customer("CLI-001", "hashed", "USER", true,
                new PersonData("Jose Updated", "Male", 32, "0102030405", "Otavalo sn y principal", "099999999"));
        var response = new CustomerResponse("CLI-001", "Jose Updated", "Male", 32,
                "0102030405", "Otavalo sn y principal", "099999999", "USER", true);

        when(repository.findByCustomerId("CLI-001")).thenReturn(Optional.of(customer));
        when(mapper.mergePatch(patchRequest, customer)).thenReturn(patched);
        when(repository.save(any(Customer.class))).thenReturn(patched);
        when(mapper.toResponse(patched)).thenReturn(response);

        StepVerifier.create(customerUseCase.patch("CLI-001", patchRequest))
                .assertNext(r -> {
                    assertThat(r.name()).isEqualTo("Jose Updated");
                    assertThat(r.phone()).isEqualTo("099999999");
                })
                .verifyComplete();
    }
}

