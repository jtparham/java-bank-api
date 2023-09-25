package com.demo.Bank;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BankAccountRepository extends JpaRepository<BankAccount, Long> {
    @Query("SELECT a.balance, SUM(a.balance) AS balance FROM BankAccount a JOIN Customer c ON c.id = :id WHERE a.customer_id = c.id GROUP BY a.id")
    List<BigDecimal> getBalances(@Param("id") Long id);
}