package com.example.mdtool.config;

import com.example.mdtool.domain.MdUserData;
import com.example.mdtool.repository.MdUserDataRepository;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class MdUserDetailsService implements UserDetailsService {

    private final MdUserDataRepository mdUserDataRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        MdUserData mdUserData = mdUserDataRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(username));

        return  MdUserDetails.customBuilder()
                .username(mdUserData.getUsername())
                .password(mdUserData.getPassword()) // {noop} で平文パスワード
                .displayName(mdUserData.getDisplayName())
                .authorities(AuthorityUtils.createAuthorityList(mdUserData.getRoles()))
                .build();
    }
}
