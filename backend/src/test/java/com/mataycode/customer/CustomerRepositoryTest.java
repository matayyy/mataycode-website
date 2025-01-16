package com.mataycode.customer;

import com.mataycode.AbstractTestcontainers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class CustomerRepositoryTest extends AbstractTestcontainers {

    @Autowired
    private CustomerRepository underTest;

    @BeforeEach
    void setUp() {
        //In main new customer is added at start application.
        underTest.deleteAll();
    }

    @Test
    void existsCustomerByEmail() {
        //GIVEN
        String email = FAKER.internet().safeEmailAddress() + "-" + UUID.randomUUID();
        Customer customer = new Customer(
                FAKER.name().fullName(),
                email,
                20,
                Gender.MALE);
        underTest.save(customer);

        //WHEN
        var actual = underTest.existsCustomerByEmail(email);

        //THEN
        assertThat(actual).isTrue();
    }

    @Test
    void existsCustomerByEmailFailsWhenEmailNotPresent() {
        //GIVEN
        String email = FAKER.internet().safeEmailAddress() + "-" + UUID.randomUUID();

        //WHEN
        var actual = underTest.existsCustomerByEmail(email);

        //THEN
        assertThat(actual).isFalse();
    }

    @Test
    void existsCustomerById() {
        //GIVEN
        String email = FAKER.internet().safeEmailAddress() + "-" + UUID.randomUUID();
        Customer customer = new Customer(
                FAKER.name().fullName(),
                email,
                20,
                Gender.MALE);
        underTest.save(customer);

        int id = underTest.findAll()
                .stream()
                .filter(c -> c.getEmail().equals(email))
                .map(Customer::getId)
                .findFirst()
                .orElseThrow();

        //WHEN
        var actual = underTest.existsCustomerById(id);

        //THEN
        assertThat(actual).isTrue();
    }

    @Test
    void existsCustomerByIdFailsWhenIdNotPresent() {
        //GIVEN
        int id = -1;
        //WHEN
        var actual = underTest.existsCustomerById(id);

        //THEN
        assertThat(actual).isFalse();
    }
}