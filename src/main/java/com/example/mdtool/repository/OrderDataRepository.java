package com.example.mdtool.repository;

import com.example.mdtool.domain.OrderData;
import com.example.mdtool.domain.StockData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderDataRepository extends JpaRepository<OrderData, Long> {
    Optional<List<OrderData>> findByBrandCode(String brandCode);
}

