package com.example.mdtool.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SalesData {

    @Id
    private String ids;

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
    private String mall;
    private String itemHashCode;

}
