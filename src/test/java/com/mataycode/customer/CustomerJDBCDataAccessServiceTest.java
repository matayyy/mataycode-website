package com.mataycode.customer;

import com.mataycode.AbstractTestcontainers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.as;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class CustomerJDBCDataAccessServiceTest extends AbstractTestcontainers {

    private CustomerJDBCDataAccessService underTest;
    private final CustomerRowMapper customerRowMapper = new CustomerRowMapper();

    @BeforeEach
    void setUp() {
        underTest = new CustomerJDBCDataAccessService(
                getJdbcTemplate(),
                customerRowMapper
        );
    }

    @Test
    void selectAllCustomers() {
        //GIVEN
        Customer customer = new Customer(
                FAKER.name().fullName(),
                FAKER.internet().safeEmailAddress() + "-" + UUID.randomUUID(),
                20
        );
        underTest.insertCustomer(customer);
        //WHEN
        List<Customer> actual = underTest.selectAllCustomers();

        //THEN
        assertThat(actual).isNotEmpty();
    }

    @Test
    void selectCustomerById() {
        //GIVEN
        String email = FAKER.internet().safeEmailAddress() + "-" + UUID.randomUUID();
        Customer customer = new Customer(
                FAKER.name().fullName(),
                email,
                20
        );
        underTest.insertCustomer(customer);

        int id = underTest.selectAllCustomers()
                .stream()
                .filter(c -> c.getEmail().equals(email))
                .map(Customer::getId)
                .findFirst()
                .orElseThrow();

        //WHEN
        Optional<Customer> actual = underTest.selectCustomerById(id);

        //THEN
        assertThat(actual).isPresent().hasValueSatisfying( c -> {
            assertThat(c.getId()).isEqualTo(id);
            assertThat(c.getName()).isEqualTo(customer.getName());
            assertThat(c.getEmail()).isEqualTo(email);
            assertThat(c.getAge()).isEqualTo(customer.getAge());
        });
    }

    @Test
    void willReturnEmptyWhenSelectCustomerById() {
        //GIVEN
        int id = -1;

        //WHEN
        var actual = underTest.selectCustomerById(id);

        //THEN
        assertThat(actual).isEmpty();
    }

    @Test
    void existPersonWithEmail() {
        //GIVEN
        String email = FAKER.internet().safeEmailAddress() + "-" + UUID.randomUUID();
        Customer customer = new Customer(
                FAKER.name().fullName(), email, 23
        );
        underTest.insertCustomer(customer);

        //WHEN
        var actual = underTest.existPersonWithEmail(email);

        //THEN
        assertThat(actual).isTrue();
    }

    @Test
    void existsPersonWithEmailWillReturnFalseWhenDoesNotExists() {
        //GIVEN
        String email = FAKER.internet().safeEmailAddress() + "-" + UUID.randomUUID();

        //WHEN
        boolean actual = underTest.existPersonWithEmail(email);

        //THEN
        assertThat(actual).isFalse();
    }

    @Test
    void existCustomerWithId() {
        //GIVEN
        String email = FAKER.internet().safeEmailAddress() + "-" + UUID.randomUUID();
        Customer customer = new Customer(
                "Sola", email, 23
        );
        underTest.insertCustomer(customer);

        int id = underTest.selectAllCustomers().stream()
                .filter(c -> c.getEmail().equals(email))
                .map(Customer::getId)
                .findFirst()
                .orElseThrow();

        //WHEN
        var actual = underTest.existPersonWithId(id);

        //THEN
        assertThat(actual).isTrue();
    }

    @Test
    void existsCustomerWithIdWillReturnFalseWhenIdNotPresent() {
        //GIVEN
        int id = -1;

        //WHEN
        boolean actual = underTest.existPersonWithId(id);

        //THEN
        assertThat(actual).isFalse();
    }

    @Test
    void deleteCustomerById() {
        //GIVEN
        String email = FAKER.internet().safeEmailAddress() + "-" + UUID.randomUUID();
        Customer customer = new Customer(
                "Pola", email, 23
        );
        underTest.insertCustomer(customer);

        int id = underTest.selectAllCustomers().stream()
                .filter(c -> c.getEmail().equals(email))
                .map(Customer::getId)
                .findFirst()
                .orElseThrow();

        //WHEN
        underTest.deleteCustomerById(id);

        //THEN
        assertThat(underTest.selectCustomerById(id)).isEmpty();
    }

    @Test
    void updateCustomerName() {
        //GIVEN
        String email = FAKER.internet().safeEmailAddress() + "-" + UUID.randomUUID();
        Customer customer = new Customer(
                FAKER.name().fullName(), email, 23
        );
        underTest.insertCustomer(customer);

        int id = underTest.selectAllCustomers().stream()
                .filter(c -> c.getEmail().equals(email))
                .map(Customer::getId)
                .findFirst()
                .orElseThrow();

        var newName = "Kola Nola";

        //WHEN change is name
        Customer update = new Customer();
        update.setId(id);
        update.setName(newName);

        underTest.updateCustomer(update);

        //THEN
        Optional<Customer> actual = underTest.selectCustomerById(id);

        assertThat(actual).isPresent().hasValueSatisfying( c -> {
            assertThat(c.getId()).isEqualTo(id);
            assertThat(c.getName()).isEqualTo(newName); //change
            assertThat(c.getEmail()).isEqualTo(email);
            assertThat(c.getAge()).isEqualTo(customer.getAge());
        });
    }

    @Test
    void updateCustomerEmail() {
        //GIVEN
        String email = FAKER.internet().safeEmailAddress() + "-" + UUID.randomUUID();
        Customer customer = new Customer(
                FAKER.name().fullName(), email, 23
        );
        underTest.insertCustomer(customer);

        int id = underTest.selectAllCustomers().stream()
                .filter(c -> c.getEmail().equals(email))
                .map(Customer::getId)
                .findFirst()
                .orElseThrow();

        var newEmail = FAKER.internet().safeEmailAddress() + "-" + UUID.randomUUID();

        //WHEN change is email
        Customer update = new Customer();
        update.setId(id);
        update.setEmail(newEmail);

        underTest.updateCustomer(update);

        //THEN
        Optional<Customer> actual = underTest.selectCustomerById(id);

        assertThat(actual).isPresent().hasValueSatisfying( c -> {
            assertThat(c.getId()).isEqualTo(id);
            assertThat(c.getName()).isEqualTo(customer.getName());
            assertThat(c.getEmail()).isEqualTo(newEmail); //change
            assertThat(c.getAge()).isEqualTo(customer.getAge());
        });
    }

    @Test
    void updateCustomerAge() {
        //GIVEN
        String email = FAKER.internet().safeEmailAddress() + "-" + UUID.randomUUID();
        Customer customer = new Customer(
                FAKER.name().fullName(), email, 23
        );
        underTest.insertCustomer(customer);

        int id = underTest.selectAllCustomers().stream()
                .filter(c -> c.getEmail().equals(email))
                .map(Customer::getId)
                .findFirst()
                .orElseThrow();

        var newAge = 24;

        //WHEN change is age
        Customer update = new Customer();
        update.setId(id);
        update.setAge(newAge);

        underTest.updateCustomer(update);

        //THEN
        Optional<Customer> actual = underTest.selectCustomerById(id);

        assertThat(actual).isPresent().hasValueSatisfying( c -> {
            assertThat(c.getId()).isEqualTo(id);
            assertThat(c.getName()).isEqualTo(customer.getName());
            assertThat(c.getEmail()).isEqualTo(email);
            assertThat(c.getAge()).isEqualTo(newAge); //change
        });
    }

    @Test
    void willUpdateAllPropertiesCustomer() {
        //GIVEN
        String email = FAKER.internet().safeEmailAddress() + "-" + UUID.randomUUID();
        Customer customer = new Customer(
                FAKER.name().fullName(), email, 23
        );
        underTest.insertCustomer(customer);

        int id = underTest.selectAllCustomers().stream()
                .filter(c -> c.getEmail().equals(email))
                .map(Customer::getId)
                .findFirst()
                .orElseThrow();

        var newName = "Kola Nola";
        var newEmail = FAKER.internet().safeEmailAddress() + "-" + UUID.randomUUID();
        var newAge = 24;

        //WHEN update with new name, email, age
        Customer update = new Customer();
        update.setId(id);
        update.setName(newName);
        update.setEmail(newEmail);
        update.setAge(newAge);

        underTest.updateCustomer(update);

        //THEN
        Optional<Customer> actual = underTest.selectCustomerById(id);

        assertThat(actual).isPresent().hasValueSatisfying( c -> {
            assertThat(c.getId()).isEqualTo(id);
            assertThat(c.getName()).isEqualTo(newName); //change
            assertThat(c.getEmail()).isEqualTo(newEmail); //change
            assertThat(c.getAge()).isEqualTo(newAge); //change
        });
    }

    @Test
    void willNotUpdateWhenNothingToUpdate() {
        //GIVEN
        String email = FAKER.internet().safeEmailAddress() + "-" + UUID.randomUUID();
        Customer customer = new Customer(
                FAKER.name().fullName(), email, 23
        );
        underTest.insertCustomer(customer);

        int id = underTest.selectAllCustomers().stream()
                .filter(c -> c.getEmail().equals(email))
                .map(Customer::getId)
                .findFirst()
                .orElseThrow();


        //WHEN update with no changes
        Customer update = new Customer();
        update.setId(id);

        underTest.updateCustomer(update);

        //THEN
        Optional<Customer> actual = underTest.selectCustomerById(id);

        assertThat(actual).isPresent().hasValueSatisfying( c -> {
            assertThat(c.getId()).isEqualTo(id);
            assertThat(c.getName()).isEqualTo(customer.getName());
            assertThat(c.getEmail()).isEqualTo(customer.getEmail());
            assertThat(c.getAge()).isEqualTo(customer.getAge());
        });
    }
}