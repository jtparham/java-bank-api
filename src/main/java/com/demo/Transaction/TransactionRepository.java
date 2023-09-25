package com.demo.Transaction;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    @Query("SELECT t FROM Transaction t JOIN Customer c ON c.id = t.sender_account_id  OR  c.id = t.receiver_account_id WHERE c.id = :id")
    List<Transaction> findAllByCustomerId(@Param("id") Long id);
}