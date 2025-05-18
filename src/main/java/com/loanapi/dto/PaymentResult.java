package com.loanapi.dto;

import java.math.BigDecimal;

public class PaymentResult {
    public final int installmentsPaid;
    public final BigDecimal totalPaid;
    public final boolean loanFullyPaid;

    public PaymentResult(int installmentsPaid, BigDecimal totalPaid, boolean loanFullyPaid) {
        this.installmentsPaid = installmentsPaid;
        this.totalPaid = totalPaid;
        this.loanFullyPaid = loanFullyPaid;
    }

    public int getInstallmentsPaid() {
        return installmentsPaid;
    }

    public BigDecimal getTotalPaid() {
        return totalPaid;
    }

    public boolean isLoanFullyPaid() {
        return loanFullyPaid;
    }
}
