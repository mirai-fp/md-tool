package com.example.mdtool.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;

import java.time.LocalDate;

@Data
@Entity
public class StockData {

    @Id
    private String id;

    private String shopName;
    private String parentCategory;
    private String childCategory;
    private String parentProductType;
    private String brandCode;
    private String productName;
    private String csCode;
    private String color;
    private String size;
    private Double sellingPrice;
    private String priceType;
    private Integer stock;
    private String itemHashCode;
    private LocalDate date;
}
