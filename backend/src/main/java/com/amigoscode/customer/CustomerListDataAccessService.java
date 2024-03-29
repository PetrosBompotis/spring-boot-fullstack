package com.amigoscode.customer;

import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


@Repository("list")
public class CustomerListDataAccessService implements CustomerDAO{

    private static List<Customer> customers;

    static{
        customers = new ArrayList<>();

        Customer alex = new Customer(1L, "Alex", "Alex@gmail.com", "password", 21, Gender.MALE);
        customers.add(alex);
        Customer Jamila = new Customer(2L, "Jamila", "Jamila@gmail.com", "password", 19, Gender.FEMALE);
        customers.add(Jamila);
    }

    @Override
    public List<Customer> selectAllCustomers() {
        return customers;
    }

    @Override
    public Optional<Customer> selectCustomerByID(Long id) {
        return customers.stream().filter(c ->  c.getId().equals(id)).findFirst();
    }

    @Override
    public void insertCustomer(Customer customer) {
        customers.add(customer);
    }

    @Override
    public boolean existsPersonWithEmail(String email) {
        return customers.stream().anyMatch(c ->  c.getEmail().equals(email));
    }

    @Override
    public void deleteCustomerById(Long customerId) {
        customers.stream().filter(c -> c.getId().equals(customerId)).findFirst().ifPresent(customers::remove);
    }

    @Override
    public boolean existsPersonWithId(Long id) {
        return customers.stream().anyMatch(c ->  c.getId().equals(id));
    }

    @Override
    public void updateCustomerById(Customer customer) {
        customers.add(customer);
    }

    @Override
    public Optional<Customer> selectUserByEmail(String email) {
        return customers.stream().filter(c ->  c.getUsername().equals(email)).findFirst();
    }


}
