package com.example.mdtool.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;

import java.time.LocalDate;

@Data
@Entity
public class FavoriteData {

    @Id
    private String id;

    private String parentCategory;
    private String brandCode;
    private String productName;
    private String priceType;
    private Integer salesPrice;
    private Integer favoriteCount;
    private LocalDate date;
}
