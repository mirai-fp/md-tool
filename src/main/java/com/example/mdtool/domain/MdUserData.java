package com.example.mdtool.domain;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MdUserData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** ログイン時に一意となるユーザー名 */
    @Column(nullable = false, unique = true, length = 50)
    private String username;

    /** BCrypt 等でハッシュ化したパスワード */
    @Column(nullable = false, length = 100)
    private String password;

    /** 表示用のお名前 */
    @Column(name = "display_name", length = 100)
    private String displayName;

    /** Spring Security 用フラグ */
    @Column(nullable = false)
    private boolean enabled = true;

    @Column(name = "account_non_expired", nullable = false)
    private boolean accountNonExpired = true;

    @Column(name = "credentials_non_expired", nullable = false)
    private boolean credentialsNonExpired = true;

    @Column(name = "account_non_locked", nullable = false)
    private boolean accountNonLocked = true;

    /**
     * 権限を文字列で保持する例。
     * デフォルトでは eager にしておくと
     * UserDetailsService で取り出しやすいです。
     */
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id")
    )
    @Column(name = "role", length = 50)
    @Builder.Default
    private Set<String> roles = new HashSet<>();
}
