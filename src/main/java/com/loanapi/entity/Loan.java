package com.loanapi.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "loans")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Loan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @Column(name = "loan_amount")
    private BigDecimal loanAmount;

    @Column(name = "total_loan_amount")
    private BigDecimal totalLoanAmount;

    @Column(name = "interest_rate")
    private BigDecimal interestRate;

    @Column(name = "number_of_installments")
    private Integer numberOfInstallments;

    @Column(name = "create_date")
    private LocalDate createDate;

    @Column(name = "is_paid")
    private Boolean isPaid = false;
}
