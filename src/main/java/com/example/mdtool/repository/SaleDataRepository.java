package com.example.mdtool.repository;

import com.example.mdtool.domain.SalesData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface SaleDataRepository extends JpaRepository<SalesData, Long> {

    @Query("SELECT s FROM SalesData s WHERE s.orderDate BETWEEN :startDate AND :endDate")
    List<SalesData> findByDateRange(LocalDate startDate, LocalDate endDate);

    SalesData findByIds(String ids);
}

