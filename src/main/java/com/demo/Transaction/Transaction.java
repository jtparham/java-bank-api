package com.demo.Transaction;

import java.math.BigDecimal;

import jakarta.persistence.*;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "transactions")
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JoinColumn(name = "sender_account_id")
    private Long sender_account_id;

    @JoinColumn(name = "receiver_account_id")
    private Long receiver_account_id;

    @Column(name = "amount")
    private BigDecimal amount;

    @Column(name = "transaction_details")
    private String transactionDetails;

    public String toString() {
        return transactionDetails;
    }
}
