package com.example.mdtool.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;

import java.time.LocalDate;

@Data
@Entity
public class OrderData {

    @Id
    private String id;

    private String brandCode;
    private String productName;
    private String color;
    private String size;
    private Integer sellingPrice;
    private Integer wholesalePrice;
    private Integer orderQuantity;
    private LocalDate orderDate;
    private String barcode;
    private String itemHashCode;
}
