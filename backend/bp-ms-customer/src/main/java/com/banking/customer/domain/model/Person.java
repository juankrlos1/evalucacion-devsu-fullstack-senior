package com.banking.customer.domain.model;

import lombok.Getter;

/**
 * Base class representing a person with personal information.
 */
@Getter
public class Person {

    private final String name;
    private final String gender;
    private final Integer age;
    private final String identification;
    private final String address;
    private final String phone;

    protected Person(String name, String gender, Integer age,
                     String identification, String address, String phone) {
        this.name = name;
        this.gender = gender;
        this.age = age;
        this.identification = identification;
        this.address = address;
        this.phone = phone;
    }
}
