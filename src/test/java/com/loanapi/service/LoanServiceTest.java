package com.loanapi.service;

import com.loanapi.dto.PaymentResult;
import com.loanapi.entity.Customer;
import com.loanapi.entity.Loan;
import com.loanapi.entity.LoanInstallment;
import com.loanapi.exception.LoanApiException;
import com.loanapi.repository.CustomerRepository;
import com.loanapi.repository.LoanInstallmentRepository;
import com.loanapi.repository.LoanRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
        import static org.mockito.Mockito.*;

class LoanServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private LoanRepository loanRepository;

    @Mock
    private LoanInstallmentRepository installmentRepository;

    @InjectMocks
    private LoanService loanService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCreateLoan_Success() {
        Customer customer = new Customer();
        customer.setId(1L);
        customer.setCreditLimit(BigDecimal.valueOf(10000));
        customer.setUsedCreditLimit(BigDecimal.ZERO);

        Loan loan = new Loan();
        loan.setLoanAmount(BigDecimal.valueOf(5000));
        loan.setNumberOfInstallments(12);
        loan.setInterestRate(BigDecimal.valueOf(0.2));

        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(loanRepository.save(any(Loan.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(installmentRepository.saveAll(anyList())).thenReturn(null);

        Loan createdLoan = loanService.createLoan(1L, loan);

        assertNotNull(createdLoan);
        assertEquals(BigDecimal.valueOf(6000.0), createdLoan.getTotalLoanAmount());
        verify(customerRepository).save(customer);
        verify(loanRepository).save(loan);
        verify(installmentRepository).saveAll(anyList());
    }

    @Test
    void testListLoans() {
        Loan loan1 = new Loan();
        Loan loan2 = new Loan();
        when(loanRepository.findByCustomerId(1L)).thenReturn(List.of(loan1, loan2));

        List<Loan> loans = loanService.listLoans(1L);

        assertEquals(2, loans.size());
        verify(loanRepository).findByCustomerId(1L);
    }

    @Test
    void testListInstallments() {
        LoanInstallment installment1 = new LoanInstallment();
        LoanInstallment installment2 = new LoanInstallment();
        when(installmentRepository.findByLoanIdAndIsPaidFalseOrderByDueDateAsc(1L))
                .thenReturn(List.of(installment1, installment2));

        List<LoanInstallment> installments = loanService.listInstallments(1L);

        assertEquals(2, installments.size());
        verify(installmentRepository).findByLoanIdAndIsPaidFalseOrderByDueDateAsc(1L);
    }

    @Test
    void testPayLoan_Success() throws LoanApiException {
        Loan loan = new Loan();
        loan.setId(1L);
        loan.setLoanAmount(BigDecimal.valueOf(2000));
        loan.setIsPaid(false);


        LoanInstallment installment1 = new LoanInstallment();
        installment1.setLoan(loan);
        installment1.setId(1L);
        installment1.setAmount(BigDecimal.valueOf(1000));
        installment1.setIsPaid(false);
        installment1.setDueDate(LocalDate.now());

        LoanInstallment installment2 = new LoanInstallment();
        installment2.setLoan(loan);
        installment2.setId(2L);
        installment2.setAmount(BigDecimal.valueOf(1000));
        installment2.setIsPaid(false);
        installment2.setDueDate(LocalDate.now());


        when(loanRepository.findById(1L)).thenReturn(Optional.of(loan));
        when(installmentRepository.findByLoanIdAndIsPaidFalseOrderByDueDateAsc(1L))
                .thenReturn(List.of(installment1,installment2));

        PaymentResult result = loanService.payLoan(1L, BigDecimal.valueOf(1000));

        assertEquals(1, result.getInstallmentsPaid());
        assertEquals(BigDecimal.valueOf(1000), result.getTotalPaid());
        assertFalse(result.isLoanFullyPaid());
        verify(installmentRepository).saveAll(anyList());
    }

    @Test
    void testPayLoan_InsufficientPayment() {
        Loan loan = new Loan();
        loan.setId(1L);

        LoanInstallment installment1 = new LoanInstallment();
        installment1.setId(1L);
        installment1.setAmount(BigDecimal.valueOf(1000));
        installment1.setIsPaid(false);
        installment1.setDueDate(LocalDate.now());

        when(loanRepository.findById(1L)).thenReturn(Optional.of(loan));
        when(installmentRepository.findByLoanIdAndIsPaidFalseOrderByDueDateAsc(1L))
                .thenReturn(List.of(installment1));

        assertThrows(LoanApiException.class, () -> loanService.payLoan(1L, BigDecimal.valueOf(500)));
    }
}