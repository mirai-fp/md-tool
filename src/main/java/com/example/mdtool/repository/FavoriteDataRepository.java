package com.example.mdtool.repository;

import com.example.mdtool.domain.FavoriteData;
import com.example.mdtool.domain.SalesData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface FavoriteDataRepository extends JpaRepository<FavoriteData, Long> {

    public List<FavoriteData> findFavoriteDataByBrandCode(String brandCode);
    public Optional<FavoriteData> findFavoriteDataByBrandCodeAndDate(String brandCode, LocalDate date);
}

