package com.demo.Bank;

import java.math.BigDecimal;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "accounts")
public class BankAccount {

    public BankAccount(Long customerId, BigDecimal initialDeposit) {
        this.setCustomer_id(customerId);
        this.setBalance(initialDeposit);
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "customer_id", nullable = false)
    private Long customer_id;

    @Column(name = "balance", nullable = false, precision = 9, scale = 2, columnDefinition = "DECIMAL(9,2) default '0.00'")
    private BigDecimal balance;

    public String toString() {
        return "balance for account number " + id + ": " + balance;
    }

}
