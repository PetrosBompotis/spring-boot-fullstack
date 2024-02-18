package com.amigoscode.customer;

import com.amigoscode.exception.DuplicateResourceException;
import com.amigoscode.exception.RequestValidationException;
import com.amigoscode.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {
    private CustomerService underTest;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private CustomerDAO customerDAO;
    private final CustomerDTOMapper customerDTOMapper = new CustomerDTOMapper();

    @BeforeEach
    void setUp() {
        underTest = new CustomerService(customerDAO, customerDTOMapper, passwordEncoder);
    }

    @Test
    void getAllCustomers() {
        underTest.getAllCustomers();
        verify(customerDAO).selectAllCustomers();
    }

    @Test
    void canGetCustomer() {
        long id = 1L;
        Customer customer = new Customer(
                id, "maria", "mar@", "password", 11, Gender.MALE
        );
        when(customerDAO.selectCustomerByID(id)).thenReturn(Optional.of(customer));

        CustomerDTO expected = customerDTOMapper.apply(customer);

        CustomerDTO actual = underTest.getCustomer(id);

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void willTrowWhenGetCustomerReturnEmptyOptional() {
        long id = 0L;

        when(customerDAO.selectCustomerByID(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> underTest.getCustomer(id))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("customer with id [%s] not found".formatted(id));
    }

    @Test
    void addCustomer() {
        String email = "p@gmail.com";

        when(customerDAO.existsPersonWithEmail(email)).thenReturn(false);

        CustomerRegistrationRequest request = new CustomerRegistrationRequest(
                "petros", email, "password", 12, Gender.MALE
        );

        String passwordHash = "sfegw4w4wr2r23";

        when(passwordEncoder.encode(request.password())).thenReturn(passwordHash);

        underTest.addCustomer(request);
        ArgumentCaptor<Customer> argumentCaptor = ArgumentCaptor.forClass(
                Customer.class
        );
        verify(customerDAO).insertCustomer(argumentCaptor.capture());
        Customer capturedCustomer = argumentCaptor.getValue();

        assertThat(capturedCustomer.getId()).isNull();
        assertThat(capturedCustomer.getName()).isEqualTo(request.name());
        assertThat(capturedCustomer.getEmail()).isEqualTo(request.email());
        assertThat(capturedCustomer.getAge()).isEqualTo(request.age());
        assertThat(capturedCustomer.getPassword()).isEqualTo(passwordHash);
    }

    @Test
    void willThrowWhenEmailExistsWhileAddingACustomer() {
        String email = "p@gmail.com";

        when(customerDAO.existsPersonWithEmail(email)).thenReturn(true);

        CustomerRegistrationRequest request = new CustomerRegistrationRequest(
                "petros", email, "password", 12, Gender.MALE
        );

        assertThatThrownBy(() -> underTest.addCustomer(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessage("email already taken");

        verify(customerDAO, never()).insertCustomer(any());
    }

    @Test
    void deleteCustomerById() {
        long id = 0L;

        when(customerDAO.existsPersonWithId(id)).thenReturn(true);
        underTest.deleteCustomerById(id);

        verify(customerDAO).deleteCustomerById(id);
    }

    @Test
    void willThrowWhenIdNotExistsWhileDeletingCustomer() {
        long id = 0L;

        when(customerDAO.existsPersonWithId(id)).thenReturn(false);
        assertThatThrownBy(() -> underTest.deleteCustomerById(id))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Customer with id [%s] not found".formatted(id));

        verify(customerDAO, never()).deleteCustomerById(any());
    }

    @Test
    void canUpdateAllCustomersProperties() {
        long id = 0L;
        Customer customer = new Customer(
                id, "maria", "mar@", "password", 11, Gender.MALE
        );
        when(customerDAO.selectCustomerByID(id)).thenReturn(Optional.of(customer));

        String newEmail = "p@gmail.com";
        CustomerUpdateRequest request = new CustomerUpdateRequest(
                "petros", newEmail, 32
        );
        when(customerDAO.existsPersonWithEmail(newEmail)).thenReturn(false);

        underTest.updateCustomer(request, id);
        ArgumentCaptor<Customer> argumentCaptor = ArgumentCaptor.forClass(
                Customer.class
        );
        verify(customerDAO).updateCustomerById(argumentCaptor.capture());
        Customer capturedCustomer = argumentCaptor.getValue();

        assertThat(capturedCustomer.getId()).isEqualTo(id);
        assertThat(capturedCustomer.getName()).isEqualTo(request.name());
        assertThat(capturedCustomer.getAge()).isEqualTo(request.age());
        assertThat(capturedCustomer.getEmail()).isEqualTo(request.email());
    }

    @Test
    void canUpdateCustomersNameProperty() {
        long id = 0L;
        Customer customer = new Customer(
                id, "maria", "mar@", "password", 11, Gender.MALE
        );
        when(customerDAO.selectCustomerByID(id)).thenReturn(Optional.of(customer));

        CustomerUpdateRequest request = new CustomerUpdateRequest(
                "petros", null, null
        );

        underTest.updateCustomer(request, id);
        ArgumentCaptor<Customer> argumentCaptor = ArgumentCaptor.forClass(
                Customer.class
        );
        verify(customerDAO).updateCustomerById(argumentCaptor.capture());
        Customer capturedCustomer = argumentCaptor.getValue();

        assertThat(capturedCustomer.getName()).isEqualTo(request.name());
        assertThat(capturedCustomer.getAge()).isEqualTo(customer.getAge());
        assertThat(capturedCustomer.getEmail()).isEqualTo(customer.getEmail());
    }

    @Test
    void canUpdateCustomersEmailProperty() {
        long id = 0L;
        Customer customer = new Customer(
                id, "maria", "mar@", "password", 11, Gender.MALE
        );
        when(customerDAO.selectCustomerByID(id)).thenReturn(Optional.of(customer));

        String newEmail = "p@gmail.com";
        CustomerUpdateRequest request = new CustomerUpdateRequest(
                null, newEmail, null
        );
        when(customerDAO.existsPersonWithEmail(newEmail)).thenReturn(false);

        underTest.updateCustomer(request, id);
        ArgumentCaptor<Customer> argumentCaptor = ArgumentCaptor.forClass(
                Customer.class
        );
        verify(customerDAO).updateCustomerById(argumentCaptor.capture());
        Customer capturedCustomer = argumentCaptor.getValue();

        assertThat(capturedCustomer.getName()).isEqualTo(customer.getName());
        assertThat(capturedCustomer.getAge()).isEqualTo(customer.getAge());
        assertThat(capturedCustomer.getEmail()).isEqualTo(newEmail);
    }

    @Test
    void willThrowWhenEmailExistsWhileUpdatingCustomer() {
        long id = 0L;
        Customer customer = new Customer(
                id, "maria", "mar@", "password", 11, Gender.MALE
        );
        when(customerDAO.selectCustomerByID(id)).thenReturn(Optional.of(customer));

        String newEmail = "p@gmail.com";
        CustomerUpdateRequest request = new CustomerUpdateRequest(
                null, newEmail, null
        );
        when(customerDAO.existsPersonWithEmail(newEmail)).thenReturn(true);

        assertThatThrownBy(() -> underTest.updateCustomer(request, id))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessage("email already taken");

        verify(customerDAO, never()).updateCustomerById(any());
    }

    @Test
    void canUpdateCustomersAgeProperty() {
        long id = 0L;
        Customer customer = new Customer(
                id, "maria", "mar@", "password", 11, Gender.MALE
        );
        when(customerDAO.selectCustomerByID(id)).thenReturn(Optional.of(customer));

        CustomerUpdateRequest request = new CustomerUpdateRequest(
                null, null, 21
        );

        underTest.updateCustomer(request, id);
        ArgumentCaptor<Customer> argumentCaptor = ArgumentCaptor.forClass(
                Customer.class
        );
        verify(customerDAO).updateCustomerById(argumentCaptor.capture());
        Customer capturedCustomer = argumentCaptor.getValue();

        assertThat(capturedCustomer.getName()).isEqualTo(customer.getName());
        assertThat(capturedCustomer.getAge()).isEqualTo(request.age());
        assertThat(capturedCustomer.getEmail()).isEqualTo(customer.getEmail());
    }

    @Test
    void willTrowWhenCustomerUpdateHasNoChanges() {
        long id = 0L;
        Customer customer = new Customer(
                id, "maria", "mar@", "password", 11, Gender.MALE
        );
        when(customerDAO.selectCustomerByID(id)).thenReturn(Optional.of(customer));

        CustomerUpdateRequest request = new CustomerUpdateRequest(
                customer.getName(), customer.getEmail(), customer.getAge()
        );

        assertThatThrownBy(() -> underTest.updateCustomer(request, id))
                .isInstanceOf(RequestValidationException.class)
                .hasMessage("no data changes found");

        verify(customerDAO, never()).updateCustomerById(any());
    }
}