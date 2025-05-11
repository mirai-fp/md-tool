package com.example.mdtool.repository;

import com.example.mdtool.domain.MdUserData;
import com.example.mdtool.domain.SalesData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface MdUserDataRepository extends JpaRepository<MdUserData, Long> {
    Optional<MdUserData> findByUsername(String username);
}

