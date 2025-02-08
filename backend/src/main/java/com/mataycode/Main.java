package com.mataycode;

import com.github.javafaker.Faker;
import com.mataycode.customer.Customer;
import com.mataycode.customer.CustomerRepository;
import com.mataycode.customer.Gender;
import com.mataycode.s3.S3Buckets;
import com.mataycode.s3.S3Service;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@SpringBootApplication
@RestController
public class Main {
    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }

    @Bean
    CommandLineRunner runner(CustomerRepository customerRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            createRandomCustomer(customerRepository, passwordEncoder);
//            testBucketUploadAndDownload(s3Service, s3Buckets);
        };
    }

    private static void testBucketUploadAndDownload(S3Service s3Service, S3Buckets s3Buckets) {
        s3Service.putObject(s3Buckets.getCustomer(), "foo", "Hello-Worldd".getBytes());

        byte[] object = s3Service.getObject(s3Buckets.getCustomer(), "foo");
        System.out.println("Hooray, " + new String(object));
    }

    private static void createRandomCustomer(CustomerRepository customerRepository, PasswordEncoder passwordEncoder) {
        Faker faker  = new Faker();
        var name = faker.name();
        String firstName = name.firstName();
        String lastName = name.lastName();
        int age = faker.number().numberBetween(18, 80);
        Gender gender = age % 2 == 0 ? Gender.MALE : Gender.FEMALE;

        Customer customer = new Customer(
                firstName + " " + lastName,
                firstName.toLowerCase() + "." + lastName.toLowerCase() + "@dev.com",
                passwordEncoder.encode(UUID.randomUUID().toString()),
                age, gender);
        customerRepository.save(customer);
    }
}
