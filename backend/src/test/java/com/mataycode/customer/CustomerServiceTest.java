package com.mataycode.customer;

import com.mataycode.exception.DuplicateResourceException;
import com.mataycode.exception.RequestValidationException;
import com.mataycode.exception.ResourceNotFoundException;
import com.mataycode.s3.S3Buckets;
import com.mataycode.s3.S3Service;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.multipart.MultipartFile;
import org.testcontainers.shaded.org.checkerframework.checker.units.qual.C;

import java.io.IOError;
import java.io.IOException;
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
    @Mock
    private S3Service s3Service;
    @Mock
    private S3Buckets s3Buckets;

    @BeforeEach
    void setUp() {
        autoCloseable = MockitoAnnotations.openMocks(this);
        underTest = new CustomerService(customerDao, customerDTOMapper, passwordEncoder, s3Service, s3Buckets);
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
                .hasMessage("Customer with id [%s] not found".formatted(id));
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
                .hasMessage("Customer with email [%s] already exists".formatted(request.email()));

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
                .hasMessage("Customer with id [%s] not found".formatted(id));

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
                .hasMessage("Customer with email [%s] already exists".formatted(updateRequest.email()));

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

    @Test
    void canUploadCustomerProfileImage() {
        //GIVEN
        int customerId = 10;
        when(customerDao.existPersonWithId(customerId)).thenReturn(true);

        //Mock multipart file
        byte[] bytes = "helloWorld".getBytes();
        MultipartFile multipartFile = new MockMultipartFile("file", bytes);

        String bucket = "customer-bucket";
        when(s3Buckets.getCustomer()).thenReturn(bucket);

        //WHEN
        underTest.uploadCustomerProfileImage(customerId, multipartFile);

        //THEN
        ArgumentCaptor<String> profileImageIdArgumentCaptor = ArgumentCaptor.forClass(String.class);
        verify(customerDao).updateProfileImageId(profileImageIdArgumentCaptor.capture(), eq(customerId));

        verify(s3Service).putObject(
                bucket,
                "profile-images/%s/%s".formatted(customerId, profileImageIdArgumentCaptor.getValue()),
                bytes);
    }

    @Test
    void canNotUploadCustomerProfileImageWhenCustomerDoesNotExist() {
        //GIVEN
        int customerId = 10;
        when(customerDao.existPersonWithId(customerId)).thenReturn(false);

        //WHEN
        assertThatThrownBy(() -> underTest.uploadCustomerProfileImage(customerId, mock(MultipartFile.class)))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Customer with id [" + customerId + "] not found");

        //THEN
        verify(customerDao).existPersonWithId(customerId);
        verifyNoMoreInteractions(customerDao);
        verifyNoInteractions(s3Buckets);
    }

    @Test
    void cannotUploadCustomerProfileImageWhenExceptionIsThrown() throws IOException {
        //GIVEN
        int customerId = 10;
        when(customerDao.existPersonWithId(customerId)).thenReturn(true);

        MultipartFile multipartFile = mock(MultipartFile.class);
        when(multipartFile.getBytes()).thenThrow(IOException.class);

        String bucket = "customer-bucket";
        when(s3Buckets.getCustomer()).thenReturn(bucket);

        //WHEN
        assertThatThrownBy(() -> underTest.uploadCustomerProfileImage(customerId, multipartFile))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Failed to upload profile image")
                .hasRootCauseInstanceOf(IOException.class);

        //THEN
        verify(customerDao, never()).updateProfileImageId(any(), any());
    }

    @Test
    void canGetCustomerProfileImage() {
        //GIVEN
        int customerId = 10;
        String profileImageId = "2222";
        Customer customer = new Customer(customerId, "Agata", "agatka@mataycode.com", "password", 23, Gender.FEMALE, profileImageId);
        when(customerDao.selectCustomerById(customerId)).thenReturn(Optional.of(customer));

        String bucket = "customer-bucket";
        when(s3Buckets.getCustomer()).thenReturn(bucket);

        when(s3Service.getObject(
                bucket,
                "profile-images/%s/%s".formatted(customerId, profileImageId)))
                .thenReturn("image".getBytes());
        //WHEN
        byte[] actualImage = underTest.getCustomerProfileImage(customerId);

        //THEN
        assertThat(actualImage).isEqualTo("image".getBytes());
    }

    @Test
    void cannotGetCustomerProfileImagerWhenNoProfileImageId() {
        //GIVEN
        int customerId = 10;
        Customer customer = new Customer(customerId, "Agata", "agatka@mataycode.com", "password", 23, Gender.FEMALE);
        when(customerDao.selectCustomerById(customerId)).thenReturn(Optional.of(customer));

        //WHEN
        //THEN
        assertThatThrownBy(() -> underTest.getCustomerProfileImage(customerId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Profile image with id [%s] not found".formatted(customerId));

        verifyNoInteractions(s3Service);
        verifyNoInteractions(s3Buckets);
    }

    @Test
    void cannotGetCustomerProfileImageWhenCustomerDoesNotExist() {
        //GIVEN
        int customerId = 10;
        when(customerDao.selectCustomerById(customerId)).thenReturn(Optional.empty());

        //WHEN
        //THEN
        assertThatThrownBy(() -> underTest.getCustomerProfileImage(customerId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Customer with id [%s] not found".formatted(customerId));

        verifyNoInteractions(s3Service);
        verifyNoInteractions(s3Buckets);
    }
}