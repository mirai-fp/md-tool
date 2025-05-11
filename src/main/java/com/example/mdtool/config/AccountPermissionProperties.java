package com.example.mdtool.config;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.springframework.util.CollectionUtils.isEmpty;

@Data
@Component
@ConfigurationProperties(prefix = "account-permissions")
public class AccountPermissionProperties {
    private Map<String, List<String>> permissions = new HashMap<>();

    public boolean isBrandAllowed(String account, String brand) {
        List<String> allowedBrands = permissions.get(account);
        return allowedBrands != null && allowedBrands.contains(brand);
    }

    public List<String> getAllowedBrands(String account) {
        return !isEmpty(permissions.get(account)) ? (new ArrayList<>(permissions.get(account + "-brand"))) : List.of();
    }
}
