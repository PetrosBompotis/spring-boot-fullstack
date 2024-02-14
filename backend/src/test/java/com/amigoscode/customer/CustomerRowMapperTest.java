package com.amigoscode.customer;


import org.junit.jupiter.api.Test;

import java.sql.ResultSet;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CustomerRowMapperTest {


    @Test
    void mapRow() throws SQLException {
        CustomerRowMapper customerRowMapper = new CustomerRowMapper();
        ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.getLong("id")).thenReturn(1L);
        when(resultSet.getInt("age")).thenReturn(15);
        when(resultSet.getString("name")).thenReturn("Jamila");
        when(resultSet.getString("email")).thenReturn("J@gmail.com");
        when(resultSet.getString("gender")).thenReturn("MALE");

        Customer actual = customerRowMapper.mapRow(resultSet, 1);

        Customer expected = new Customer(1L, "Jamila", "J@gmail.com", "password", 15, Gender.MALE);

        assertThat(actual).isEqualTo(expected);
    }
}