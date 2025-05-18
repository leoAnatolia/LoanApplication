
package com.loanapi.controller;

import com.loanapi.dto.ErrorResponse;
import com.loanapi.dto.PaymentResult;
import com.loanapi.entity.Loan;
import com.loanapi.entity.LoanInstallment;
import com.loanapi.exception.LoanApiException;
import com.loanapi.service.LoanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/v1/loans")
public class LoanController {
    @Autowired
    private LoanService loanService;


    @PostMapping("/{customerId}")
    public ResponseEntity<?> createLoan(@PathVariable Long customerId,

                                           @RequestBody Loan loan
    ) {

        try {
            Loan createdLoan = loanService.createLoan(customerId, loan);

            return ResponseEntity.ok(createdLoan);

        } catch (IllegalArgumentException e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse( "Illegal argument", e.getMessage()));
        }

    }

    @GetMapping("/{customerId}")
    public ResponseEntity<?> listLoans(@PathVariable Long customerId) {

        if (isNullOrLessThanOne(customerId)) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse("Invalid customer ID", "Customer ID must be a positive number"));
        }

        try {
            List<Loan> loans = loanService.listLoans(customerId);
            return ResponseEntity.ok(loans);
        } catch (IllegalArgumentException e) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse("Customer not found", e.getMessage()));
        }
    }

    @GetMapping("/{loanId}/installments")
    public ResponseEntity<?> listInstallments(@PathVariable Long loanId) {

        if (isNullOrLessThanOne(loanId)) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse("Invalid loan ID", "Loan ID must be a positive number"));
        }

        try {
            List<LoanInstallment> installments = loanService.listInstallments(loanId);
            return ResponseEntity.ok(installments);
        } catch (IllegalArgumentException e) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse("Loan not found", e.getMessage()));
        }
    }

    @PostMapping("/{loanId}/pay")
    public ResponseEntity<?> payLoan(
            @PathVariable Long loanId,
            @RequestParam(required = true) BigDecimal paymentAmount
    ) {

        if (isNullOrLessThanOne(loanId)) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse("Invalid loan ID", "Loan ID must be a positive number"));
        }

        if (paymentAmount == null || paymentAmount.compareTo(BigDecimal.ZERO) <= 0) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse("Invalid payment amount", "Payment amount must be a positive number"));
        }


        try {
            PaymentResult result = loanService.payLoan(loanId, paymentAmount);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse("Payment failed", e.getMessage()));
        } catch (LoanApiException e) {
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(new ErrorResponse("Payment failed", e.getMessage()));
        }
    }

    private static boolean isNullOrLessThanOne(Long Id) {
        return Id == null || Id < 1;
    }
}
