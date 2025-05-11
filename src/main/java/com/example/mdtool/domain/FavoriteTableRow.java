package com.example.mdtool.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FavoriteTableRow {
    private Map<String, Integer> weeklyFavorite; // key: "2024-W39"
}
