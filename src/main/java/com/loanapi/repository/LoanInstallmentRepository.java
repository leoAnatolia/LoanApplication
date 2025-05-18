
package com.loanapi.repository;

import com.loanapi.entity.LoanInstallment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LoanInstallmentRepository extends JpaRepository<LoanInstallment, Long> {
    List<LoanInstallment> findByLoanIdAndIsPaidFalseOrderByDueDateAsc(Long loanId);
}
