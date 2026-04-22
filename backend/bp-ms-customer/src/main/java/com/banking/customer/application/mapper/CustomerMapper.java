package com.banking.customer.application.mapper;

import com.banking.customer.application.dto.CustomerPatchRequest;
import com.banking.customer.application.dto.CustomerRequest;
import com.banking.customer.application.dto.CustomerResponse;
import com.banking.customer.domain.model.Customer;
import com.banking.customer.domain.model.PersonData;
import org.mapstruct.Mapper;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface CustomerMapper {

    default Customer toDomain(String customerId, CustomerRequest req) {
        var data = new PersonData(req.name(), req.gender(), req.age(),
                req.identification(), req.address(), req.phone());
        return new Customer(customerId, req.password(), "USER", req.active(), data);
    }

    default CustomerResponse toResponse(Customer c) {
        return new CustomerResponse(
                c.getCustomerId(), c.getName(), c.getGender(), c.getAge(),
                c.getIdentification(), c.getAddress(), c.getPhone(), c.getRole(), c.isActive()
        );
    }

    @Named("mergePatch")
    default Customer mergePatch(CustomerPatchRequest patch, Customer existing) {
        var data = new PersonData(
                patch.name() != null ? patch.name() : existing.getName(),
                patch.gender() != null ? patch.gender() : existing.getGender(),
                patch.age() != null ? patch.age() : existing.getAge(),
                patch.identification() != null ? patch.identification() : existing.getIdentification(),
                patch.address() != null ? patch.address() : existing.getAddress(),
                patch.phone() != null ? patch.phone() : existing.getPhone()
        );
        return new Customer(
                existing.getCustomerId(),
                patch.password() != null ? patch.password() : existing.getPassword(),
                existing.getRole(),
                patch.active() != null ? patch.active() : existing.isActive(),
                data
        );
    }
}
