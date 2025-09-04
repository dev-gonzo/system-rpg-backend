package br.com.systemrpg.backend.service;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import br.com.systemrpg.backend.domain.entity.Role;
import br.com.systemrpg.backend.domain.entity.User;
import br.com.systemrpg.backend.exception.RecordNotFoundException;
import br.com.systemrpg.backend.repository.RoleRepository;
import br.com.systemrpg.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;

/**
 * Serviço responsável pela lógica de negócio relacionada aos usuários.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final MessageSource messageSource;

    /**
     * Busca um usuário por ID.
     */
    public User findById(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RecordNotFoundException(messageSource
                        .getMessage("service.user.not.found", new Object[]{id}, LocaleContextHolder.getLocale())));
    }

    /**
     * Lista todos os usuários com paginação, excluindo usuários deletados.
     */
    public Page<User> findAll(Pageable pageable) {
        return userRepository.findByDeletedAtIsNullOrderByCreatedAtDesc(pageable);
    }

    /**
     * Lista usuários ativos com paginação, excluindo usuários deletados.
     */
    public Page<User> findActiveUsers(Pageable pageable) {
        return userRepository.findByIsActiveTrueAndDeletedAtIsNullOrderByCreatedAtDesc(pageable);
    }

    /**
     * Busca usuários por username, nome, sobrenome ou email.
     */
    public Page<User> findByName(String searchTerm, Pageable pageable) {
        return userRepository.findByUsernameOrFirstNameOrLastNameOrEmailContainingIgnoreCase(searchTerm, pageable);
    }



    /**
     * Cria um novo usuário.
     */
    @Transactional
    public User createUser(User user, Set<String> roleNames) {

        if (userRepository.existsByUsername(user.getUsername())) {
            throw new IllegalArgumentException(messageSource
                    .getMessage("service.user.username.exists", new Object[]{user.getUsername()}, LocaleContextHolder.getLocale()));
        }

        if (userRepository.existsByEmail(user.getEmail())) {
            throw new IllegalArgumentException(messageSource
                    .getMessage("service.user.email.exists", new Object[]{user.getEmail()}, LocaleContextHolder.getLocale()));
        }

        if (StringUtils.hasText(user.getPasswordHash())) {
            user.setPasswordHash(passwordEncoder.encode(user.getPasswordHash()));
        }

        user.setIsActive(true);
        user.setIsEmailVerified(false);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        user.setPasswordChangedAt(LocalDateTime.now());

        if (roleNames != null && !roleNames.isEmpty()) {
            Set<Role> roles = roleRepository.findByNameInAndIsActiveTrue(roleNames);
            if (roles.size() != roleNames.size()) {
                throw new IllegalArgumentException(messageSource
                        .getMessage("service.user.roles.not.found", null, LocaleContextHolder.getLocale()));
            }
            user.setRoles(roles);
        } else {
            Role defaultRole = roleRepository.findByName("USER")
                    .orElseThrow(() -> new IllegalStateException(messageSource
                            .getMessage("service.user.role.default.not.found", null, LocaleContextHolder.getLocale())));
            if (user.getRoles() == null) {
                user.setRoles(new HashSet<>());
            }
            user.getRoles().add(defaultRole);
        }
        
        return userRepository.save(user);
    }

    /**
     * Atualiza um usuário existente.
     */
    @Transactional
    public User updateUser(UUID id, User updatedUser, Set<String> roleNames) {
        
        User existingUser = findById(id);

        if (!existingUser.getUsername().equals(updatedUser.getUsername()) && 
            userRepository.existsByUsernameAndIdNot(updatedUser.getUsername(), id)) {
            throw new IllegalArgumentException(messageSource
                    .getMessage("service.user.username.exists", new Object[]{updatedUser.getUsername()}, LocaleContextHolder.getLocale()));
        }

        if (!existingUser.getEmail().equals(updatedUser.getEmail()) && 
            userRepository.existsByEmailAndIdNot(updatedUser.getEmail(), id)) {
            throw new IllegalArgumentException(messageSource
                    .getMessage("service.user.email.exists", new Object[]{updatedUser.getEmail()}, LocaleContextHolder.getLocale()));
        }

        existingUser.setUsername(updatedUser.getUsername());
        existingUser.setEmail(updatedUser.getEmail());
        existingUser.setFirstName(updatedUser.getFirstName());
        existingUser.setLastName(updatedUser.getLastName());
        existingUser.setUpdatedAt(LocalDateTime.now());


        if (StringUtils.hasText(updatedUser.getPasswordHash())) {
            existingUser.setPasswordHash(passwordEncoder.encode(updatedUser.getPasswordHash()));
            existingUser.setPasswordChangedAt(LocalDateTime.now());
        }

        if (roleNames != null) {
            existingUser.getRoles().clear();
            if (!roleNames.isEmpty()) {
                Set<Role> roles = roleRepository.findByNameInAndIsActiveTrue(roleNames);
                if (roles.size() != roleNames.size()) {
                    throw new IllegalArgumentException(messageSource
                            .getMessage("service.user.roles.not.found", null, LocaleContextHolder.getLocale()));
                }
                existingUser.setRoles(roles);
            }
        }
        
        return userRepository.save(existingUser);
    }

    /**
     * Ativa ou desativa um usuário.
     */
    @Transactional
    public User toggleUserStatus(UUID id, Boolean isActive) {
        
        User user = findById(id);
        user.setIsActive(isActive);
        user.setUpdatedAt(LocalDateTime.now());
        
        return userRepository.save(user);
    }

    /**
     * Exclui um usuário (soft delete - marca como deletado).
     */
    @Transactional
    public void deactivateUser(UUID id) {
        
        User user = findById(id);
        user.setDeletedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        
        userRepository.save(user);
    }

    /**
     * Exclui permanentemente um usuário.
     */
    @Transactional
    public void deleteUser(UUID id) {
        
        User user = findById(id);
        userRepository.delete(user);

    }

    /**
     * Verifica se um username está disponível.
     */
    public boolean isUsernameAvailable(String username) {
        return !userRepository.existsByUsername(username);
    }

    /**
     * Verifica se um email está disponível.
     */
    public boolean isEmailAvailable(String email) {
        return !userRepository.existsByEmail(email);
    }

    /**
     * Verifica o status de verificação de email.
     */
    @Transactional
    public User verifyEmail(UUID id) {
        log.info("Verificando email do usuário com ID: {}", id);
        
        User user = findById(id);
        user.setIsEmailVerified(true);
        user.setUpdatedAt(LocalDateTime.now());
        
        return userRepository.save(user);

    }
}
