
package com.loanapi.service;

import com.loanapi.dto.PaymentResult;
import com.loanapi.entity.Customer;
import com.loanapi.entity.Loan;
import com.loanapi.entity.LoanInstallment;
import com.loanapi.exception.LoanApiException;
import com.loanapi.repository.CustomerRepository;
import com.loanapi.repository.LoanRepository;
import com.loanapi.repository.LoanInstallmentRepository;
import jakarta.transaction.Transactional;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Service
public class LoanService {

    private static final Log logger = LogFactory.getLog(LoanService.class);

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private LoanRepository loanRepository;

    @Autowired
    private LoanInstallmentRepository installmentRepository;

    // Validate loan creation parameters
    private void validateLoanCreation(Customer customer, Loan loan) {
        // Check credit limit
        if (customer.getCreditLimit().subtract(customer.getUsedCreditLimit()).compareTo(loan.getLoanAmount()) < 0) {
            throw new IllegalArgumentException("Insufficient credit limit");
        }

        // Check valid installment numbers
        if (!List.of(6, 9, 12, 24).contains(loan.getNumberOfInstallments())) {
            throw new IllegalArgumentException("Invalid number of installments");
        }

        // Check interest rate
        BigDecimal interestRate = loan.getInterestRate();
        if (interestRate.compareTo(BigDecimal.valueOf(0.1)) < 0 ||
                interestRate.compareTo(BigDecimal.valueOf(0.5)) > 0) {
            throw new IllegalArgumentException("Interest rate must be between 0.1 and 0.5");
        }
    }

    @Transactional
    public Loan createLoan(Long customerId,Loan loan) {
        // Fetch customer
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found"));

        // Validate loan parameters
        validateLoanCreation(customer, loan);

        // Calculate total loan amount with interest
        BigDecimal totalLoanAmount = loan.getLoanAmount().multiply(BigDecimal.ONE.add(loan.getInterestRate()));


        loan.setTotalLoanAmount(totalLoanAmount);
        loan.setCustomer(customer);
        loan.setCreateDate(LocalDate.now());

        // Save loan
        loan = loanRepository.save(loan);


        // Create installments
        BigDecimal installmentAmount = loan.getTotalLoanAmount().divide(
                BigDecimal.valueOf(loan.getNumberOfInstallments()), 2, BigDecimal.ROUND_HALF_UP);

        List<LoanInstallment> installments = new ArrayList<>();
        LocalDate firstInstallmentDate = LocalDate.now().plusMonths(1).withDayOfMonth(1);

        for (int i = 0; i < loan.getNumberOfInstallments(); i++) {
            LoanInstallment installment = new LoanInstallment();
            installment.setLoan(loan);
            installment.setAmount(installmentAmount);
            installment.setPaidAmount(BigDecimal.ZERO);
            installment.setDueDate(firstInstallmentDate.plusMonths(i));
            installment.setIsPaid(false);

            installments.add(installment);
        }

        installmentRepository.saveAll(installments);

        // Update customer's used credit limit
        customer.setUsedCreditLimit(customer.getUsedCreditLimit().add(loan.getLoanAmount()));
        customerRepository.save(customer);

        return loan;
    }

    public List<Loan> listLoans(Long customerId) {
        return loanRepository.findByCustomerId(customerId);
    }

    public List<LoanInstallment> listInstallments(Long loanId) {
        return installmentRepository.findByLoanIdAndIsPaidFalseOrderByDueDateAsc(loanId);
    }

    @Transactional
    public PaymentResult payLoan(Long loanId, BigDecimal paymentAmount) throws LoanApiException {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new IllegalArgumentException("Loan not found"));

        List<LoanInstallment> unpaidInstallments = installmentRepository
                .findByLoanIdAndIsPaidFalseOrderByDueDateAsc(loanId);

        int installmentsPaid = 0;
        BigDecimal totalPaid = BigDecimal.ZERO;
        LocalDate now = LocalDate.now();

        for (LoanInstallment installment : unpaidInstallments) {


            // Check if payment covers full installment
            if (paymentAmount.compareTo(installment.getAmount()) >= 0) {

                // Check if installment is within payable period (not more than 3 months ahead)
                if (installment.getDueDate().isAfter(now.plusMonths(3))) {
                    logger.info("Installment " + installment.getId() + " is not due yet.");
                    throw new LoanApiException("Installment " + installment.getId() + " is not due yet.");

                }

                // Update installment
                installment.setIsPaid(true);
                //installment.setPaidAmount(adjustedAmount);
                installment.setPaymentDate(now);

                // Reduce payment amount
                paymentAmount = paymentAmount.subtract(installment.getAmount());

                // Increment counters
                installmentsPaid++;
                totalPaid = totalPaid.add(installment.getAmount());

                logger.info("Paid installment: " + installment.getId() + ", Amount: " + installment.getAmount());


            } else if(installmentsPaid==0) {
                // Not enough to pay full installment
                logger.info("Not enough to pay full installment: " + installment.getId() + ", Amount: " + paymentAmount);
                throw new LoanApiException("Not enough to pay full installment: " + installment.getId());
            }
        }

        // Save updated installments
        installmentRepository.saveAll(unpaidInstallments);

        // Check if loan is fully paid
        long remainingUnpaidInstallments = unpaidInstallments.stream()
                .filter(i -> !i.getIsPaid())
                .count();

        if (remainingUnpaidInstallments == 0) {
            loan.setIsPaid(true);
            loanRepository.save(loan);
        }

        return new PaymentResult(installmentsPaid, totalPaid, loan.getIsPaid());
    }

    private BigDecimal calculateAdjustedAount(LoanInstallment installment, LocalDate paymentDate) {
        BigDecimal baseAmount = installment.getAmount();
        long daysDifference = ChronoUnit.DAYS.between(installment.getDueDate(), paymentDate);

        if (daysDifference < 0) {
            // Early payment - apply discount
            return baseAmount.subtract(
                    baseAmount.multiply(BigDecimal.valueOf(0.001 * Math.abs(daysDifference)))
            );
        } else if (daysDifference > 0) {
            // Late payment - apply penalty
            return baseAmount.add(
                    baseAmount.multiply(BigDecimal.valueOf(0.001 * daysDifference))
            );
        }

        return baseAmount;
    }
}
