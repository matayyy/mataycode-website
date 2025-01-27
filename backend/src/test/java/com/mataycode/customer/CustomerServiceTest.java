package com.mataycode.customer;

import com.mataycode.exception.DuplicateResourceException;
import com.mataycode.exception.RequestValidationException;
import com.mataycode.exception.ResourceNotFoundException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class CustomerServiceTest {

    @Mock
    private CustomerDao customerDao;
    private CustomerService underTest;
    private AutoCloseable autoCloseable;
    private CustomerDTOMapper customerDTOMapper = new CustomerDTOMapper();
    @Mock
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        autoCloseable = MockitoAnnotations.openMocks(this);
        underTest = new CustomerService(customerDao, customerDTOMapper, passwordEncoder);
    }

    @AfterEach
    void tearDown() throws Exception {
        autoCloseable.close();
    }

    @Test
    void getAllCustomers() {
        //WHEN
        underTest.getAllCustomers();

        //THEN
        verify(customerDao).selectAllCustomers();
    }

    @Test
    void getCustomerById() {
        //GIVEN
        int id = 10;
        Customer customer = new Customer(id, "Luna", "luna@dev.com", "password", 23, Gender.MALE);
        when(customerDao.selectCustomerById(id)).thenReturn(Optional.of(customer));

        CustomerDTO expected = customerDTOMapper.apply(customer);

        //WHEN
        CustomerDTO actual = underTest.getCustomerById(id);

        //THEN
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void willThrowWhenGetCustomerByIdReturnEmptyOptional() {
        //GIVEN
        int id = 10;
        when(customerDao.selectCustomerById(id)).thenReturn(Optional.empty());

        //WHEN
        //THEN
        assertThatThrownBy(() -> underTest.getCustomerById(id))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Customer with id [%s] not found" .formatted(id));
    }

    @Test
    void addCustomer() {
        //GIVEN
        String email = "luna@dev.com";
        when(customerDao.existPersonWithEmail(email)).thenReturn(false);

        CustomerRegistrationRequest request = new CustomerRegistrationRequest(
                "Luna", email, "password", 23, Gender.FEMALE
        );

        String passwordHash = "G@#RGGGASD@!#A123";
        when(passwordEncoder.encode(request.password())).thenReturn(passwordHash);

        //WHEN
        underTest.addCustomer(request);

        //THEN
        ArgumentCaptor<Customer> customerArgumentCaptor = ArgumentCaptor.forClass(Customer.class);
        verify(customerDao).insertCustomer(customerArgumentCaptor.capture());

        Customer capturedCustomer = customerArgumentCaptor.getValue();
        assertThat(capturedCustomer.getId()).isNull();
        assertThat(capturedCustomer.getName()).isEqualTo(request.name());
        assertThat(capturedCustomer.getEmail()).isEqualTo(request.email());
        assertThat(capturedCustomer.getAge()).isEqualTo(request.age());
        assertThat(capturedCustomer.getPassword()).isEqualTo(passwordHash);
    }

    @Test
    void willThrowEmailExistsWhileAddingCustomer() {
        //GIVEN
        String email = "luna@dev.com";
        when(customerDao.existPersonWithEmail(email)).thenReturn(true);

        CustomerRegistrationRequest request = new CustomerRegistrationRequest(
                "Luna", email, "password", 23, Gender.FEMALE
        );

        //WHEN
        //THEN
        assertThatThrownBy(() -> underTest.addCustomer(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessage("Customer with email [%s] already exists" .formatted(request.email()));

        verify(customerDao, never()).insertCustomer(any());
    }

    @Test
    void deleteCustomerById() {
        //GIVEN
        int id = 10;
        when(customerDao.existPersonWithId(id)).thenReturn(true);

        //WHEN
        underTest.deleteCustomerById(id);

        //THEN
        verify(customerDao).deleteCustomerById(id);
    }

    @Test
    void willThrowDeleteCustomerByIdNotExists() {
        //GIVEN
        int id = 10;
        when(customerDao.existPersonWithId(id)).thenReturn(false);

        //WHEN
        assertThatThrownBy(() -> underTest.deleteCustomerById(id))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Customer with id [%s] not found" .formatted(id));

        //THEN
        verify(customerDao, never()).deleteCustomerById(id);
    }

    @Test
    void canUpdateAllFieldsCustomer() {
        //GIVEN
        int id = 10;
        Customer customerToUpdate = new Customer(id, "Luna", "luna@dev.com", "password", 23, Gender.MALE);
        when(customerDao.selectCustomerById(id)).thenReturn(Optional.of(customerToUpdate));

        CustomerUpdateRequest updateRequest = new CustomerUpdateRequest("Sola", "sola@dev.com", 24, Gender.FEMALE);
        when(customerDao.existPersonWithEmail(updateRequest.email())).thenReturn(false);

        //WHEN
        underTest.updateCustomer(id, updateRequest);

        //THEN
        ArgumentCaptor<Customer> customerArgumentCaptor = ArgumentCaptor.forClass(Customer.class);
        verify(customerDao).updateCustomer(customerArgumentCaptor.capture());
        Customer capturedCustomer = customerArgumentCaptor.getValue();

        assertThat(capturedCustomer.getId()).isEqualTo(id);
        assertThat(capturedCustomer.getName()).isEqualTo(updateRequest.name());
        assertThat(capturedCustomer.getEmail()).isEqualTo(updateRequest.email());
        assertThat(capturedCustomer.getAge()).isEqualTo(updateRequest.age());
        assertThat(capturedCustomer.getGender()).isEqualTo(updateRequest.gender());
    }

    @Test
    void canUpdateCustomerName() {
        //GIVEN
        int id = 10;
        CustomerUpdateRequest updateRequest = new CustomerUpdateRequest("Sola", null, null, null);
        Customer customerToUpdate = new Customer(id, "Luna", "luna@dev.com", "password", 23, Gender.MALE);

        when(customerDao.selectCustomerById(id)).thenReturn(Optional.of(customerToUpdate));

        //WHEN
        underTest.updateCustomer(id, updateRequest);

        //THEN
        ArgumentCaptor<Customer> customerArgumentCaptor = ArgumentCaptor.forClass(Customer.class);
        verify(customerDao).updateCustomer(customerArgumentCaptor.capture());
        Customer capturedCustomer = customerArgumentCaptor.getValue();

        assertThat(capturedCustomer.getId()).isEqualTo(id);
        assertThat(capturedCustomer.getName()).isEqualTo(updateRequest.name());
        assertThat(capturedCustomer.getEmail()).isEqualTo(customerToUpdate.getEmail());
        assertThat(capturedCustomer.getAge()).isEqualTo(customerToUpdate.getAge());
        assertThat(capturedCustomer.getGender()).isEqualTo(customerToUpdate.getGender());
    }

    @Test
    void canUpdateCustomerEmail() {
        //GIVEN
        int id = 10;
        CustomerUpdateRequest updateRequest = new CustomerUpdateRequest(null, "sola@dev.com", null, null);
        Customer customerToUpdate = new Customer(id, "Luna", "luna@dev.com", "password", 23, Gender.MALE);

        when(customerDao.selectCustomerById(id)).thenReturn(Optional.of(customerToUpdate));
        when(customerDao.existPersonWithEmail(updateRequest.email())).thenReturn(false);

        //WHEN
        underTest.updateCustomer(id, updateRequest);

        //THEN
        ArgumentCaptor<Customer> customerArgumentCaptor = ArgumentCaptor.forClass(Customer.class);
        verify(customerDao).updateCustomer(customerArgumentCaptor.capture());
        Customer capturedCustomer = customerArgumentCaptor.getValue();

        assertThat(capturedCustomer.getId()).isEqualTo(id);
        assertThat(capturedCustomer.getName()).isEqualTo(customerToUpdate.getName());
        assertThat(capturedCustomer.getEmail()).isEqualTo(updateRequest.email());
        assertThat(capturedCustomer.getAge()).isEqualTo(customerToUpdate.getAge());
    }

    @Test
    void canUpdateCustomerAge() {
        //GIVEN
        int id = 10;
        CustomerUpdateRequest updateRequest = new CustomerUpdateRequest(null, null, 24, null);
        Customer customerToUpdate = new Customer(id, "Luna", "luna@dev.com", "password", 23, Gender.MALE);

        when(customerDao.selectCustomerById(id)).thenReturn(Optional.of(customerToUpdate));

        //WHEN
        underTest.updateCustomer(id, updateRequest);

        //THEN
        ArgumentCaptor<Customer> customerArgumentCaptor = ArgumentCaptor.forClass(Customer.class);
        verify(customerDao).updateCustomer(customerArgumentCaptor.capture());
        Customer capturedCustomer = customerArgumentCaptor.getValue();

        assertThat(capturedCustomer.getId()).isEqualTo(id);
        assertThat(capturedCustomer.getName()).isEqualTo(customerToUpdate.getName());
        assertThat(capturedCustomer.getEmail()).isEqualTo(customerToUpdate.getEmail());
        assertThat(capturedCustomer.getAge()).isEqualTo(updateRequest.age());
    }

    @Test
    void willThrowWhenUpdateCustomerEmailWhichIsTaken() {
        //GIVEN
        int id = 10;
        CustomerUpdateRequest updateRequest = new CustomerUpdateRequest(null, "sola@dev.com", null, null);
        Customer customerToUpdate = new Customer(id, "Luna", "luna@dev.com", "password", 23, Gender.MALE);

        when(customerDao.selectCustomerById(id)).thenReturn(Optional.of(customerToUpdate));
        when(customerDao.existPersonWithEmail(updateRequest.email())).thenReturn(true);

        //WHEN
        assertThatThrownBy(() -> underTest.updateCustomer(id, updateRequest))
            .isInstanceOf(DuplicateResourceException.class)
                    .hasMessage("Customer with email [%s] already exists" .formatted(updateRequest.email()));

        //THEN
        verify(customerDao, never()).updateCustomer(any());
    }

    @Test
    void willThrowWhenNotchingToUpdateCustomer() {
        //GIVEN
        int id = 10;
        Customer customerToUpdate = new Customer(id, "Luna", "luna@dev.com", "password", 23, Gender.MALE);
        CustomerUpdateRequest updateRequest = new CustomerUpdateRequest(
                customerToUpdate.getName(), customerToUpdate.getEmail(), customerToUpdate.getAge(), customerToUpdate.getGender());

        when(customerDao.selectCustomerById(id)).thenReturn(Optional.of(customerToUpdate));
        when(customerDao.existPersonWithEmail(updateRequest.email())).thenReturn(false);

        //WHEN
        assertThatThrownBy(() -> underTest.updateCustomer(id, updateRequest))
                .isInstanceOf(RequestValidationException.class)
                .hasMessage("No data changes found");

        //THEN
        verify(customerDao, never()).updateCustomer(any());
    }
}