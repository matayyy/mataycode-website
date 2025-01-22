package com.mataycode.customer;

import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerDao {

    List<Customer> selectAllCustomers();
    Optional<Customer> selectCustomerById(Integer id);
    void insertCustomer(Customer customer);
    boolean existPersonWithEmail(String email);
    boolean existPersonWithId(Integer id);
    void deleteCustomerById(Integer id);
    void updateCustomer(Customer update);
    Optional<Customer> selectUserByEmail(String email);
}
