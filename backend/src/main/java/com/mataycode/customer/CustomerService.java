package com.mataycode.customer;

import com.mataycode.exception.DuplicateResourceException;
import com.mataycode.exception.RequestValidationException;
import com.mataycode.exception.ResourceNotFoundException;
import com.mataycode.s3.S3Buckets;
import com.mataycode.s3.S3Service;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class CustomerService {

    private final CustomerDao customerDao;
    private final CustomerDTOMapper customerDTOMapper;
    private final PasswordEncoder passwordEncoder;
    private final S3Service s3Service;
    private final S3Buckets s3Buckets;

    public CustomerService(@Qualifier("jdbc") CustomerDao customerDao, CustomerDTOMapper customerDTOMapper, PasswordEncoder passwordEncoder, S3Service s3Service, S3Buckets s3Buckets) {
        this.customerDao = customerDao;
        this.customerDTOMapper = customerDTOMapper;
        this.passwordEncoder = passwordEncoder;
        this.s3Service = s3Service;
        this.s3Buckets = s3Buckets;
    }

    public List<CustomerDTO> getAllCustomers() {
        return customerDao.selectAllCustomers()
                .stream()
                .map(customerDTOMapper)
                .collect(Collectors.toList());
    }

    public CustomerDTO getCustomerById(Integer id) {
        return customerDao.selectCustomerById(id)
                .map(customerDTOMapper)
                .orElseThrow(
                        () -> new ResourceNotFoundException("Customer with id [%s] not found".formatted(id))
                );
    }

    public void addCustomer(CustomerRegistrationRequest customerRegistrationRequest) {
        //check if email exist
        if (customerDao.existPersonWithEmail(customerRegistrationRequest.email())) {
            throw new DuplicateResourceException("Customer with email [%s] already exists".formatted(customerRegistrationRequest.email()));
        }
        //add
        customerDao.insertCustomer(
                new Customer(
                        customerRegistrationRequest.name(),
                        customerRegistrationRequest.email(),
                        passwordEncoder.encode(customerRegistrationRequest.password()),
                        customerRegistrationRequest.age(),
                        customerRegistrationRequest.gender()));
    }

    public void deleteCustomerById(Integer customerId) {
        checkIfCustomerExistsOrThrow(customerId);
        //delete
        customerDao.deleteCustomerById(customerId);
    }

    private void checkIfCustomerExistsOrThrow(Integer customerId) {
        //check if customer exists
        if (!customerDao.existPersonWithId(customerId)) {
            throw new ResourceNotFoundException("Customer with id [%s] not found".formatted(customerId));
        }
    }

    public void updateCustomer(Integer customerId, CustomerUpdateRequest updateRequest) {
        //get customer
        Customer customerToUpdate = customerDao.selectCustomerById(customerId)
                .orElseThrow(
                        () -> new ResourceNotFoundException("Customer with id [%s] not found".formatted(customerId))
                );

        boolean changes = false;

        //check field name
        if (!customerToUpdate.getName().equals(updateRequest.name()) && updateRequest.name() != null) {
            customerToUpdate.setName(updateRequest.name());
            changes = true;
        }

        //check field email
        if (!customerToUpdate.getEmail().equals(updateRequest.email()) && updateRequest.email() != null) {
            if (customerDao.existPersonWithEmail(updateRequest.email())) {
                throw new DuplicateResourceException("Customer with email [%s] already exists".formatted(updateRequest.email()));
            }
            customerToUpdate.setEmail(updateRequest.email());
            changes = true;
        }

        //check field age
        if (!customerToUpdate.getAge().equals(updateRequest.age()) && updateRequest.age() != null) {
            customerToUpdate.setAge(updateRequest.age());
            changes = true;
        }

        //check field gender
        if (!customerToUpdate.getGender().equals(updateRequest.gender()) && updateRequest.gender() != null) {
            customerToUpdate.setGender(updateRequest.gender());
            changes = true;
        }

        //update customer or throw exception
        if (changes) {
            customerDao.updateCustomer(customerToUpdate);
        } else {
            throw new RequestValidationException("No data changes found");
        }
    }

    public CustomerDTO getCustomerByEmail(String email) {
        return customerDao.selectUserByEmail(email)
                .map(customerDTOMapper)
                .orElseThrow(
                        () -> new ResourceNotFoundException("Customer with email [%s] not found".formatted(email))
                );
    }

    public void uploadCustomerProfileImage(Integer customerId, MultipartFile file) {

        checkIfCustomerExistsOrThrow(customerId);
        String profileImageId = UUID.randomUUID().toString();

        try {
            s3Service.putObject(s3Buckets.getCustomer(), "profile-images/%s/%s".formatted(customerId, profileImageId), file.getBytes());
        } catch (IOException e) {
            throw new RuntimeException("Failed to upload profile image", e);
        }

        //Store profileImageId to db
        customerDao.updateProfileImageId(profileImageId, customerId);
    }

    public byte[] getCustomerProfileImage(Integer customerId) {
        CustomerDTO customer = customerDao.selectCustomerById(customerId)
                .map(customerDTOMapper)
                .orElseThrow(
                        () -> new ResourceNotFoundException("Customer with id [%s] not found".formatted(customerId))
                );

        //check if  profileImageId is empty or null
        if (StringUtils.isBlank(customer.profileImageId())) {
            throw new ResourceNotFoundException("Profile image with id [%s] not found".formatted(customerId));
        }

        return s3Service.getObject(s3Buckets.getCustomer(), "profile-images/%s/%s".formatted(customerId, customer.profileImageId()));
    }
}
