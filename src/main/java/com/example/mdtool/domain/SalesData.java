package com.example.mdtool.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;

import java.time.LocalDate;

@Data
@Entity
public class SalesData {

    @Id
    private String id;

    private String shopName;
    private String parentCategory;
    private String childCategory;
    private String parentProductType;
    private String childProductType;
    private String gender;
    private String brandCode;
    private String productName;
    private String csCode;
    private String color;
    private String size;
    private Double sellingPrice;
    private String sellingType;
    private String priceType;
    private Double originalPrice;
    private Integer orderQuantity;
    private Double totalAmount;
    private LocalDate orderDate;
    private String barcode;
    private String itemHashCode;
}
