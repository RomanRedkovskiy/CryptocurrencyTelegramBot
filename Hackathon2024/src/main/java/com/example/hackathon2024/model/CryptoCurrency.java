package com.example.hackathon2024.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;

@Data
@Entity(name = "cryptos")
public class CryptoCurrency {

    @Id
    private String symbol;

    private Double price;
}
