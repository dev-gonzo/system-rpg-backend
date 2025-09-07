package br.com.systemrpg.backend.service;

import br.com.systemrpg.backend.domain.entity.User;
import br.com.systemrpg.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementação do UserDetailsService para autenticação Spring Security.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserDetailsServiceImpl implements UserDetailsService {

    private static final Logger log = LoggerFactory.getLogger(UserDetailsServiceImpl.class);
    private final UserRepository userRepository;
    private final MessageSource messageSource;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.debug("Tentando carregar usuário: {}", username);
        
        User user = userRepository.findByUsernameOrEmail(username)
            .orElseThrow(() -> {
                log.warn("Usuário não encontrado: {}", username);
                return new UsernameNotFoundException(
                    messageSource
                            .getMessage("service.userdetails.user.not.found", new Object[]{username}, LocaleContextHolder.getLocale()));
            });

        log.info("Usuário encontrado: {}, ativo: {}, hash presente: {}, hash completo: {}", 
                 user.getUsername(), user.getIsActive(), 
                 user.getPasswordHash() != null ? "SIM" : "NÃO",
                 user.getPasswordHash());

        UserDetails userDetails = org.springframework.security.core.userdetails.User.builder()
            .username(user.getUsername())
            .password(user.getPasswordHash())
            .disabled(!user.getIsActive())
            .accountExpired(false)
            .accountLocked(false)
            .credentialsExpired(false)
            .authorities(getAuthorities(user))
            .build();
            
        log.debug("UserDetails criado para: {}, authorities: {}", 
                 userDetails.getUsername(), userDetails.getAuthorities());
                 
        return userDetails;
    }

    /**
     * Converte as roles do usuário em authorities do Spring Security.
     */
    private Collection<? extends GrantedAuthority> getAuthorities(User user) {
        return user.getRoles().stream()
            .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getName().toUpperCase()))
            .collect(Collectors.toList());
    }
}
