package com.example.mdtool.config;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;

@Getter
class MdUserDetails extends User {

    /** 画面に表示するユーザーの名称 */
    private final String displayName;

    /**
     * コンストラクタは protected にして、Builder 経由でのみインスタンス化させる
     */
    protected MdUserDetails(
            String username,
            String password,
            boolean enabled,
            boolean accountNonExpired,
            boolean credentialsNonExpired,
            boolean accountNonLocked,
            Collection<? extends GrantedAuthority> authorities,
            String displayName
    ) {
        super(username, password, enabled, accountNonExpired, credentialsNonExpired, accountNonLocked, authorities);
        this.displayName = displayName;
    }

    /** 静的ファクトリーメソッドで Builder を呼び出す */
    public static Builder customBuilder() {
        return new Builder();
    }

    /** Builder クラス */
    public static class Builder {
        private String username;
        private String password;
        private boolean enabled = true;
        private boolean accountNonExpired = true;
        private boolean credentialsNonExpired = true;
        private boolean accountNonLocked = true;
        private Collection<? extends GrantedAuthority> authorities = java.util.Collections.emptyList();
        private String displayName;

        public Builder username(String username) {
            this.username = username;
            return this;
        }
        public Builder password(String password) {
            this.password = password;
            return this;
        }
        public Builder enabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }
        public Builder accountNonExpired(boolean accountNonExpired) {
            this.accountNonExpired = accountNonExpired;
            return this;
        }
        public Builder credentialsNonExpired(boolean credentialsNonExpired) {
            this.credentialsNonExpired = credentialsNonExpired;
            return this;
        }
        public Builder accountNonLocked(boolean accountNonLocked) {
            this.accountNonLocked = accountNonLocked;
            return this;
        }
        public Builder authorities(Collection<? extends GrantedAuthority> authorities) {
            this.authorities = authorities;
            return this;
        }
        public Builder displayName(String displayName) {
            this.displayName = displayName;
            return this;
        }

        public MdUserDetails build() {
            return new MdUserDetails(
                    this.username,
                    this.password,
                    this.enabled,
                    this.accountNonExpired,
                    this.credentialsNonExpired,
                    this.accountNonLocked,
                    this.authorities,
                    this.displayName
            );
        }
    }
}