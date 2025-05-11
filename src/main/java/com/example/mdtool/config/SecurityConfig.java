package com.example.mdtool.config;

import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, MdUserDetailsService mdUserDetailsService, PasswordEncoder passwordEncoder) throws Exception {
        http
                .userDetailsService(mdUserDetailsService)
                .authenticationProvider(daoAuthenticationProvider(mdUserDetailsService, passwordEncoder))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/h2-console/**").permitAll()
                        //        .anyRequest().permitAll()
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login") // カスタムログインページ（後述）
                        .permitAll()
                )
                // ログアウト設定
                .logout(logout -> logout
                        // ログアウト用URL。デフォルトは POST /logout
                        .logoutUrl("/logout")
                        // ログアウト後のリダイレクト先
                        .logoutSuccessUrl("/login")
                        // セッション破棄
                        .invalidateHttpSession(true)
                        // SecurityContext クリア
                        .clearAuthentication(true)
                        // クッキー削除
                        .deleteCookies("JSESSIONID")
                        // だれでもアクセス可
                        .permitAll())
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers("/h2-console/**") // ★ CSRF無効化（H2 Console用）

                ).headers(headers -> headers.frameOptions().sameOrigin() // iframe制限を緩和
                );

        return http.build();
    }

    /** BCrypt アルゴリズムでハッシュ化＆比較します */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider daoAuthenticationProvider(
            MdUserDetailsService mdUserDetailsService,
            PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(mdUserDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

//    @Bean
//    public UserDetailsService users() {
//        MdUserDetails user = MdUserDetails.customBuilder()
//                .username("corp-wa")
//                .password("{noop}wA-2o25o4") // {noop} で平文パスワード
//                .displayName("株式会社WA様")
//                .authorities(AuthorityUtils.createAuthorityList("READ", "WRITE"))
//                .build();
//        return new InMemoryUserDetailsManager(user);
//    }
}