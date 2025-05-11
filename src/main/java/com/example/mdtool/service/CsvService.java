package com.example.mdtool.service;

import com.example.mdtool.config.AccountPermissionProperties;
import com.example.mdtool.domain.FavoriteData;
import com.example.mdtool.domain.OrderData;
import com.example.mdtool.domain.SalesData;
import com.example.mdtool.domain.StockData;
import com.example.mdtool.repository.FavoriteDataRepository;
import com.example.mdtool.repository.OrderDataRepository;
import com.example.mdtool.repository.SaleDataRepository;
import com.example.mdtool.repository.StockDataRepository;
import com.ibm.icu.text.CharsetDetector;
import com.ibm.icu.text.CharsetMatch;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class CsvService {

    @Autowired
    private SaleDataRepository saleDataRepository;

    @Autowired
    private StockDataRepository stockDataRepository;

    @Autowired
    private OrderDataRepository orderDataRepository;

    @Autowired
    private FavoriteDataRepository favoriteDataRepository;

    @Autowired
    private AccountPermissionProperties accountPermissionProperties;


    public List<String> processSalesCSV(MultipartFile file) throws IOException {
        List<String> result = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), detectEncoding(file)))) {
            Iterable<CSVRecord> records = CSVFormat.DEFAULT
                    .withFirstRecordAsHeader()
                    .parse(reader);

            int count = 2;
            for (CSVRecord record : records) {
                SalesData salesData = new SalesData();
                salesData.setShopName(record.get("ショップ名"));
                salesData.setParentCategory(record.get("親カテゴリ"));
                salesData.setChildCategory(record.get("子カテゴリ"));
                salesData.setParentProductType(record.get("親商品タイプ"));
                salesData.setChildProductType(record.get("子商品タイプ"));
                salesData.setGender(record.get("性別"));
                salesData.setBrandCode(record.get("ブランド品番"));
                salesData.setProductName(record.get("商品名"));
                salesData.setCsCode(record.get("CS品番"));
                salesData.setColor(record.get("カラー"));
                salesData.setSize(record.get("サイズ"));
                salesData.setSellingPrice(Double.parseDouble(record.get("販売価格（税抜）")));
                salesData.setSellingType(record.get("販売タイプ"));
                salesData.setPriceType(record.get("価格タイプ"));
                salesData.setOriginalPrice(Double.parseDouble(record.get("元上代（税抜）")));
                salesData.setOrderQuantity(Integer.parseInt(record.get("注文数")));
                salesData.setTotalAmount(Double.parseDouble(record.get("合計金額（税抜）")));
                salesData.setOrderDate(LocalDate.parse(record.get("注文日"), DateTimeFormatter.ofPattern("yyyyMMdd")));
                salesData.setBarcode(record.get("バーコード"));
                salesData.setMall(record.get("モール"));
                String id = salesData.getParentCategory() + salesData.getBrandCode() + salesData.getCsCode() + salesData.getPriceType() + salesData.getSellingType() + salesData.getSellingPrice() + salesData.getMall() + salesData.getOrderDate().toString();
                salesData.setIds(id);
                salesData.setItemHashCode(salesData.getBrandCode() + salesData.getCsCode());

                if (isAllowedBrand(salesData.getParentCategory())) {
                    saleDataRepository.save(salesData);
                }
                else {
                    result.add(count + "行目, 未許可ブランド: " + salesData.getParentCategory());
                }

                count++;
            }
        }
        return result;
    }

    public List<String> processStockCSV(MultipartFile file, LocalDate date) throws IOException {
        List<String> result = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), detectEncoding(file)))) {
            Iterable<CSVRecord> records = CSVFormat.DEFAULT
                    .withFirstRecordAsHeader()
                    .parse(reader);

            int count = 2;
            for (CSVRecord record : records) {
                StockData stockData = new StockData();
                stockData.setShopName(record.get("ショップ名"));
                stockData.setParentCategory(record.get("親カテゴリー"));
                stockData.setChildCategory(record.get("カテゴリー"));
                stockData.setParentProductType(record.get("商品タイプ"));
                stockData.setBrandCode(record.get("ブランド品番"));
                stockData.setProductName(record.get("商品名"));
                stockData.setCsCode(record.get("CS品番"));
                stockData.setColor(record.get("カラー"));
                stockData.setSize(record.get("サイズ"));
                stockData.setSellingPrice(Double.parseDouble(record.get("販売価格(税抜)")));
                stockData.setPriceType(record.get("価格タイプ"));
                stockData.setStock(Integer.parseInt(record.get("在庫数")));
                stockData.setId(stockData.getParentCategory() + stockData.getBrandCode() + stockData.getColor() + stockData.getSize());
                stockData.setDate(date);
                stockData.setItemHashCode(stockData.getBrandCode() + stockData.getCsCode());

                if (isAllowedBrand(stockData.getParentCategory())) {
                    stockDataRepository.save(stockData);
                }
                else {
                    result.add(count + "行目, 未許可ブランド: " + stockData.getParentCategory());
                }

                count++;
            }
        }
        return result;
    }

    public List<String> processOrderCSV(MultipartFile file) throws IOException {
        List<String> result = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), detectEncoding(file)))) {
            Iterable<CSVRecord> records = CSVFormat.DEFAULT
                    .withFirstRecordAsHeader()
                    .parse(reader);

            int count = 2;
            for (CSVRecord record : records) {
                if (Integer.parseInt(record.get("発注数")) == 0) {
                    continue;
                }
                OrderData orderData = new OrderData();
                orderData.setBrand(record.get("ブランド"));
                orderData.setBrandCode(record.get("品番"));
                orderData.setProductName(record.get("商品名"));
                orderData.setColor(record.get("カラー"));
                orderData.setSize(record.get("サイズ"));
                orderData.setSellingPrice(Integer.parseInt(record.get("上代")));
                orderData.setWholesalePrice(Integer.parseInt(record.get("下代")));
                orderData.setOrderDate(LocalDate.parse(record.get("発注日"), DateTimeFormatter.ofPattern("yyyy/MM/dd")));
                orderData.setOrderQuantity(Integer.parseInt(record.get("発注数")));
                orderData.setId(orderData.getBrand() + orderData.getBrandCode() + orderData.getColor() + orderData.getSize() + record.get("発注書番号"));
                orderData.setDeliveryDate(LocalDate.parse(record.get("入荷希望日"), DateTimeFormatter.ofPattern("yyyy/MM/dd")));

                if (isAllowedBrand(orderData.getBrand())) {
                    orderDataRepository.save(orderData);
                }
                else {
                    result.add("ファイル名:" + file.getOriginalFilename() + ", " + count + "行目, 未許可ブランド: " + orderData.getBrand());
                }

                count++;
            }
        }
        return result;
    }

    public List<String> processFavCSV(MultipartFile file, LocalDate date) throws IOException {
        List<String> result = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), detectEncoding(file)))) {
            Iterable<CSVRecord> records = CSVFormat.DEFAULT
                    .withFirstRecordAsHeader()
                    .parse(reader);

            int count = 2;
            for (CSVRecord record : records) {
                FavoriteData favData = new FavoriteData();
                favData.setParentCategory(record.get("親カテゴリ"));
                favData.setBrandCode(record.get("ブランド品番"));
                favData.setProductName(record.get("商品名"));
                favData.setPriceType(record.get("価格タイプ"));
                favData.setSalesPrice(Integer.parseInt(record.get("価格（税抜）").replaceAll("[,，]", "").trim()));
                favData.setFavoriteCount(Integer.parseInt(record.get("登録数")));
                favData.setDate(date);
                String id = favData.getParentCategory() + favData.getBrandCode() + favData.getPriceType() + favData.getDate().toString();
                favData.setId(id);

                if (isAllowedBrand(favData.getParentCategory())) {
                    favoriteDataRepository.save(favData);
                }
                else {
                    result.add(count + "行目, 未許可ブランド: " + favData.getParentCategory());
                }

                count++;
            }
        }
        return result;
    }

    private  String detectEncoding(MultipartFile file) throws IOException {
        try (InputStream input = new BufferedInputStream(file.getInputStream())) {
            CharsetDetector detector = new CharsetDetector();
            detector.setText(input);
            CharsetMatch match = detector.detect();
            return match != null ? match.getName() : "UTF-8"; // デフォルトUTF-8
        }
    }

    private boolean isAllowedBrand(String brandName) {
        String account = SecurityContextHolder.getContext().getAuthentication().getName();
        return accountPermissionProperties.isBrandAllowed(account, brandName);
    }

}
