package com.example.mdtool.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@AllArgsConstructor
public class CategoryMapper {

    private static final Map<String, String> CATEGORY_MAP = new HashMap<>();

    static {
        // OT
        CATEGORY_MAP.put("テーラードジャケット", "OT");
        CATEGORY_MAP.put("ダウンジャケット/コート", "OT");
        CATEGORY_MAP.put("ブルゾン", "OT");
        CATEGORY_MAP.put("その他アウター", "OT");
        CATEGORY_MAP.put("ミリタリージャケット", "OT");
        CATEGORY_MAP.put("ステンカラーコート", "OT");
        CATEGORY_MAP.put("MA-1", "OT");
        CATEGORY_MAP.put("トレンチコート", "OT");
        CATEGORY_MAP.put("デニムジャケット", "OT");
        CATEGORY_MAP.put("セットアップ", "OT");
        CATEGORY_MAP.put("ムートンコート", "OT");
        CATEGORY_MAP.put("チェスターコート", "OT");


        // KN
        CATEGORY_MAP.put("ニット/セーター", "KN");
        CATEGORY_MAP.put("カーディガン/ボレロ", "KN");
        CATEGORY_MAP.put("ベスト", "KN");
        CATEGORY_MAP.put("アンサンブル", "KN");

        // CT
        CATEGORY_MAP.put("Tシャツ/カットソー", "CT");
        CATEGORY_MAP.put("パーカー", "CT");
        CATEGORY_MAP.put("タンクトップ", "CT");
        CATEGORY_MAP.put("キャミソール", "CT");
        CATEGORY_MAP.put("スウェット", "CT");
        CATEGORY_MAP.put("チューブトップ", "CT");
        CATEGORY_MAP.put("ジャージ", "CT");

        // BL
        CATEGORY_MAP.put("シャツ/ブラウス", "BL");
        CATEGORY_MAP.put("ポロシャツ", "BL");
        CATEGORY_MAP.put("その他トップス", "BL");
        CATEGORY_MAP.put("ビジネスシャツ", "BL");

        // OP
        CATEGORY_MAP.put("ワンピース", "OP");

        // SK
        CATEGORY_MAP.put("スカート", "SK");

        // PT
        CATEGORY_MAP.put("チノパンツ", "PT");
        CATEGORY_MAP.put("スウェットパンツ", "PT");
        CATEGORY_MAP.put("その他パンツ", "PT");
        CATEGORY_MAP.put("スラックス", "PT");
        CATEGORY_MAP.put("カーゴパンツ", "PT");
        CATEGORY_MAP.put("デニムパンツ", "PT");

        // SHOES
        CATEGORY_MAP.put("シューズ", "SHOES");

        // GOODS
        CATEGORY_MAP.put("ショルダーバッグ", "GOODS");
        CATEGORY_MAP.put("ハンドバッグ", "GOODS");
        CATEGORY_MAP.put("トートバッグ", "GOODS");
        CATEGORY_MAP.put("ヘアバンド", "GOODS");
        CATEGORY_MAP.put("ネックレス", "GOODS");
        CATEGORY_MAP.put("チャーム", "GOODS");
        CATEGORY_MAP.put("ピアス（両耳用）", "GOODS");
        CATEGORY_MAP.put("ニットキャップ/ビーニー", "GOODS");
        CATEGORY_MAP.put("サングラス", "GOODS");
        CATEGORY_MAP.put("ストール/ショール", "GOODS");
        CATEGORY_MAP.put("メガネ", "GOODS");
        CATEGORY_MAP.put("ベルト", "GOODS");
        CATEGORY_MAP.put("ネックウォーマー/スヌード", "GOODS");
        CATEGORY_MAP.put("ファッション雑貨", "GOODS");
    }

    public String map(String input) {
        return CATEGORY_MAP.getOrDefault(input, "システム未定義");
    }
}

