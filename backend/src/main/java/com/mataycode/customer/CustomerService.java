package com.mataycode.customer;

import com.mataycode.exception.DuplicateResourceException;
import com.mataycode.exception.RequestValidationException;
import com.mataycode.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustomerService {

    private final CustomerDao customerDao;

    public CustomerService(@Qualifier("jdbc") CustomerDao customerDao) {
        this.customerDao = customerDao;
    }

    public List<Customer> getAllCustomers() {
        return customerDao.selectAllCustomers();
    }

    public Customer getCustomerById(Integer id) {
        return customerDao.selectCustomerById(id)
                .orElseThrow(
                        () -> new ResourceNotFoundException("Customer with id [%s] not found" .formatted(id))
                );
    }

    public void addCustomer(CustomerRegistrationRequest customerRegistrationRequest) {
        //check if email exist
        if(customerDao.existPersonWithEmail(customerRegistrationRequest.email())) {
            throw new DuplicateResourceException("Customer with email [%s] already exists" .formatted(customerRegistrationRequest.email()));
        }
        //add
        customerDao.insertCustomer(
                new Customer(
                        customerRegistrationRequest.name(),
                        customerRegistrationRequest.email(),
                        customerRegistrationRequest.age(),
                        customerRegistrationRequest.gender()));
    }

    public void deleteCustomerById(Integer customerId) {
        //check if customer exists
        if(!customerDao.existPersonWithId(customerId)) {
            throw new ResourceNotFoundException("Customer with id [%s] not found" .formatted(customerId));
        }
        //delete
        customerDao.deleteCustomerById(customerId);
    }

    public void updateCustomer(Integer customerId, CustomerUpdateRequest updateRequest) {
        //get customer
        Customer customerToUpdate = getCustomerById(customerId);
        boolean changes = false;

        //check field name
        if(!customerToUpdate.getName().equals(updateRequest.name()) && updateRequest.name() != null) {
            customerToUpdate.setName(updateRequest.name());
            changes = true;
        }

        //check field email
        if(!customerToUpdate.getEmail().equals(updateRequest.email()) && updateRequest.email() != null) {
            if(customerDao.existPersonWithEmail(updateRequest.email())) {
                throw new DuplicateResourceException("Customer with email [%s] already exists" .formatted(updateRequest.email()));
            }
            customerToUpdate.setEmail(updateRequest.email());
            changes = true;
        }

        //check field age
        if(!customerToUpdate.getAge().equals(updateRequest.age()) && updateRequest.age() != null) {
            customerToUpdate.setAge(updateRequest.age());
            changes = true;
        }

        //check field gender
        if(!customerToUpdate.getGender().equals(updateRequest.gender()) && updateRequest.gender() != null) {
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
}
