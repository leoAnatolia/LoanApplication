package com.loanapi;

import com.loanapi.repository.CustomerRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
public class LoanApplicationTests {

    @Autowired
    CustomerRepository customerRepository;


    @Test
    void contextLoads() {
        assertEquals(1, customerRepository.findAll().size());
    }
}
