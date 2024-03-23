package com.example.hackathon2024.repository;

import com.example.hackathon2024.model.CryptoCurrency;
import jakarta.transaction.Transactional;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.Set;

public interface CryptoRepository extends CrudRepository<CryptoCurrency, Long> {

    @Transactional
    @Modifying
    @Query(value = "INSERT INTO cryptos (symbol, price) VALUES (:symbol, :price) " +
            "ON DUPLICATE KEY UPDATE price = VALUES(price)", nativeQuery = true)
    void insertOrUpdateCryptoData(@Param("symbol") String symbol, @Param("price") Double price);

    @NotNull
    Set<CryptoCurrency> findAll();
}
