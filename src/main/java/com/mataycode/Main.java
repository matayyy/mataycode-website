package com.mataycode;

import com.github.javafaker.Faker;
import com.mataycode.customer.Customer;
import com.mataycode.customer.CustomerRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@SpringBootApplication
@RestController
public class Main {
    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }

    @Bean
    CommandLineRunner runner(CustomerRepository customerRepository) {
        return args -> {

            Faker faker  = new Faker();
            var name = faker.name();
            String firstName = name.firstName();
            String lastName = name.lastName();

            Customer customer = new Customer(
                    firstName + " " + lastName,
                    firstName.toLowerCase() + "." + lastName.toLowerCase() + "@dev.com",
                    faker.number().numberBetween(18,80));
            customerRepository.save(customer);
        };
    }
}
