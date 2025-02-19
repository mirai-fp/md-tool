package com.example.mdtool.repository;

import com.example.mdtool.domain.SalesData;
import com.example.mdtool.domain.StockData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface StockDataRepository extends JpaRepository<StockData, Long> {
    Optional<List<StockData>> findByBrandCode(String brandCode);
}

