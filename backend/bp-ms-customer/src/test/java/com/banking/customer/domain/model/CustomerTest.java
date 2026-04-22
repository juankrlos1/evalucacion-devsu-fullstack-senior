package com.banking.customer.domain.model;

import com.banking.customer.domain.exception.DomainException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CustomerTest {

    @Test
    void shouldCreateValidCustomer() {
        var data = new PersonData("John Doe", "Male", 30, "1234567890", "123 Main St", "555-0100");
        var customer = new Customer("CLI-001", "secret123", "USER", true, data);

        assertThat(customer.getCustomerId()).isEqualTo("CLI-001");
        assertThat(customer.getName()).isEqualTo("John Doe");
        assertThat(customer.isActive()).isTrue();
        assertThat(customer.getPassword()).isEqualTo("secret123");
        assertThat(customer.getRole()).isEqualTo("USER");
    }

    @Test
    void shouldDeactivateCustomer() {
        var data = new PersonData("John Doe", "Male", 30, "1234567890", "123 Main St", "555-0100");
        var customer = new Customer("CLI-001", "secret123", "USER", true, data);

        var deactivated = customer.deactivate();

        assertThat(deactivated.isActive()).isFalse();
        assertThat(deactivated.getCustomerId()).isEqualTo("CLI-001");
    }

    @Test
    void shouldRejectBlankName() {
        assertThatThrownBy(() -> new PersonData("", "Male", 30, "123", "addr", "phone"))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("Name is required");
    }

    @Test
    void shouldRejectBlankCustomerId() {
        var data = new PersonData("John", "Male", 30, "123", "addr", "phone");
        assertThatThrownBy(() -> new Customer("", "pass", "USER", true, data))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("Customer ID is required");
    }
}
