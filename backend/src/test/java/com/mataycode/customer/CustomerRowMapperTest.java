package com.mataycode.customer;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.ResultSet;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomerRowMapperTest {

    @Test
    void mapRow() throws SQLException {
        //GIVEN
        CustomerRowMapper customerRowMapper = new CustomerRowMapper();

        ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.getInt("id")).thenReturn(1);
        when(resultSet.getString("name")).thenReturn("John");
        when(resultSet.getString("email")).thenReturn("john@gmail.com");
        when(resultSet.getString("password")).thenReturn("password");
        when(resultSet.getInt("age")).thenReturn(21);
        when(resultSet.getString("gender")).thenReturn("MALE");
        when(resultSet.getString("profile_image_id")).thenReturn("55555");

        //WHEN
        Customer actual = customerRowMapper.mapRow(resultSet, 1);

        //THEN
        Customer expected = new Customer(1, "John", "john@gmail.com", "password", 21, Gender.MALE, "55555");
        assertThat(actual).isEqualTo(expected);
    }
}