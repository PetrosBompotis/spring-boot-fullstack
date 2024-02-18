package com.amigoscode.customer;

import com.amigoscode.exception.DuplicateResourceException;
import com.amigoscode.exception.RequestValidationException;
import com.amigoscode.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CustomerService {

    private final CustomerDAO customerDAO;
    private final CustomerDTOMapper customerDTOMapper;
    private final PasswordEncoder passwordEncoder;


    public CustomerService(@Qualifier("jpa") CustomerDAO customerDAO, CustomerDTOMapper customerDTOMapper, PasswordEncoder passwordEncoder) {
        this.customerDAO = customerDAO;
        this.customerDTOMapper = customerDTOMapper;
        this.passwordEncoder = passwordEncoder;
    }

    public List<CustomerDTO> getAllCustomers(){

        return customerDAO.selectAllCustomers().stream().map(customerDTOMapper)
                .collect(Collectors.toList());
    }

    public CustomerDTO getCustomer(Long id){
        return customerDAO.selectCustomerByID(id).map(customerDTOMapper)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "customer with id [%s] not found".formatted(id)
                ));
    }

    public void addCustomer(CustomerRegistrationRequest customerRegistrationRequest){

        String email = customerRegistrationRequest.email();
        //check if email exists
        if(customerDAO.existsPersonWithEmail(email)){
            throw new DuplicateResourceException("email already taken");
        }

        //otherwise add
        Customer customer = new Customer(
                customerRegistrationRequest.name(),
                customerRegistrationRequest.email(),
                passwordEncoder.encode(customerRegistrationRequest.password()),
                customerRegistrationRequest.age(),
                customerRegistrationRequest.gender()
        );
        customerDAO.insertCustomer(customer);

    }

    public void deleteCustomerById(Long custmerId){

        //check if id exists
        if(!customerDAO.existsPersonWithId(custmerId)){
            throw new ResourceNotFoundException("Customer with id [%s] not found".formatted(custmerId));
        }

        //otherwise remove
        customerDAO.deleteCustomerById(custmerId);

    }

    public void updateCustomer(CustomerUpdateRequest updateRequest, Long customerId){

        Customer customer = customerDAO.selectCustomerByID(customerId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "customer with id [%s] not found".formatted(customerId)
                ));
        boolean changes = false;
        //check if attributes need change exists
        if (updateRequest.name() != null && !updateRequest.name().equals(customer.getName())){
            customer.setName(updateRequest.name());
            changes = true;
        }

        if (updateRequest.age() != null && !updateRequest.age().equals(customer.getAge())){
            customer.setAge(updateRequest.age());
            changes = true;
        }

        if (updateRequest.email() != null && !updateRequest.email().equals(customer.getEmail())){
            if(customerDAO.existsPersonWithEmail(updateRequest.email())){
                throw new DuplicateResourceException("email already taken");
            }
            customer.setEmail(updateRequest.email());
            changes = true;
        }
        //otherwise update

        if (!changes){
            throw new RequestValidationException("no data changes found");
        }

        customerDAO.updateCustomerById(customer);
    }
}















