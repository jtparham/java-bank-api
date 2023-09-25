package com.demo.Bank;

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
@Table(name = "create_account_request")
public class CreateAccountRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JoinColumn(name = "customer_id", referencedColumnName = "id", insertable = false, updatable = false)
    private Long customer_id;

    private BigDecimal initialDeposit;

    public CreateAccountRequest(Long customer_id, BigDecimal initialDeposit) {
        this.customer_id = customer_id;
        this.initialDeposit = initialDeposit;
    }

}
