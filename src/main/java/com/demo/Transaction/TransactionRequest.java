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
@Table(name = "transaction_request")
public class TransactionRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "sending_customer_id", nullable = false)
    private Long sending_customer_id;

    @Column(name = "receiving_customer_id", nullable = false)
    private Long receiving_customer_id;

    @JoinColumn(name = "sender_account_id")
    private Long sender_account_id;

    @JoinColumn(name = "receiver_account_id")
    private Long receiver_account_id;

    @Column(name = "amount")
    private BigDecimal amount;

    public TransactionRequest(Long sender_account_id, Long sending_customer_id, Long receiver_account_id,
            Long receiving_customer_id, BigDecimal amount) {
        this.sender_account_id = sender_account_id;
        this.sending_customer_id = sending_customer_id;
        this.receiver_account_id = receiver_account_id;
        this.receiving_customer_id = receiving_customer_id;
        this.amount = amount;
    }

}
