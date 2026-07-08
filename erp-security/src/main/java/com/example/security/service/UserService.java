package com.example.security.service;

import com.example.erp.common.domain.status.ServiceResult;
import com.example.erp.common.domain.status.Status;
import com.example.erp.common.exception.LocalizedException;
import com.erp.common.search.DefaultFieldValueConverter;
import com.erp.common.search.SearchRequest;
import com.erp.common.search.SetAllowedFields;
import com.erp.common.search.SpecBuilder;
import com.example.erp.common.web.util.PageableValidator;
import com.example.security.entity.Role;
import com.example.security.entity.UserAccount;
import com.example.security.dto.CreateUserRequest;
import com.example.security.dto.UpdateUserRequest;
import com.example.security.dto.UserDto;
import com.example.security.exception.SecurityErrorCodes;
import com.example.security.mapper.UserMapper;
import com.example.security.repository.RoleRepository;
import com.example.security.repository.UserAccountRepository;
import com.example.security.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

/**
 * User Service with authorization at service layer (Rule 19.1)
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {

    private final UserAccountRepository repo;
    private final RoleRepository roleRepo;
    private final PasswordEncoder encoder;
    private final RefreshTokenRepository refreshTokenRepo;

    // Allowed sort fields for user listing (Rule 17.3)
    private static final Set<String> ALLOWED_USER_SORT_FIELDS = Set.of(
        "id", "username", "enabled", "createdAt"
    );

    // Allowed search fields for dynamic filtering (Rule 17.3)
    private static final Set<String> ALLOWED_USER_SEARCH_FIELDS = Set.of(
        "id", "username", "enabled", "createdAt"
    );

    @Transactional
    @PreAuthorize("hasAuthority(T(com.example.security.constants.SecurityPermissions).USER_CREATE)")
    @CacheEvict(cacheNames = "users", allEntries = true)
    public ServiceResult<UserDto> createUser(CreateUserRequest req) {
        if (repo.existsByUsernameIgnoreCase(req.username())) {
            throw new LocalizedException(Status.ALREADY_EXISTS, SecurityErrorCodes.USERNAME_ALREADY_EXISTS, req.username());
        }

        UserAccount u = UserAccount.builder()
            .username(req.username())
            .password(encoder.encode(req.password()))
            .enabled(true)
            .build();

        // ربط الدور الافتراضي
        roleRepo.findByRoleName("ROLE_USER")
            .ifPresent(r -> u.setRoles(Set.of(r)));

        UserAccount saved = repo.save(u);
        // map to DTO while transaction/session is open so lazy collections can be initialized
        return ServiceResult.success(UserMapper.toDto(saved), Status.CREATED);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority(T(com.example.security.constants.SecurityPermissions).USER_VIEW)")
    public ServiceResult<Page<UserDto>> listUsers(Pageable pageable) {
        // Validate sort fields against whitelist (Rule 17.3)
        pageable = PageableValidator.validateSortFields(pageable, ALLOWED_USER_SORT_FIELDS);

        Page<UserAccount> users = repo.findAll(pageable);
        // map to DTOs while session is open to avoid LazyInitializationException
        return ServiceResult.success(users.map(UserMapper::toDto));
    }

    /**
     * ربط مستخدم بأدوار (استبدال كامل للأدوار الحالية)
     * @param userId معرّف المستخدم
     * @param roleNames قائمة أسماء الأدوار
     * @return UserDto المستخدم بعد التحديث
     */
    @Transactional
    @PreAuthorize("hasAuthority(T(com.example.security.constants.SecurityPermissions).USER_MANAGE_ROLES)")
    @CacheEvict(cacheNames = {"users", "userRoles"}, allEntries = true)
    public ServiceResult<UserDto> assignRoles(Long userId, Set<String> roleNames) {
        UserAccount user = repo.findById(userId)
            .orElseThrow(() -> new LocalizedException(
                Status.NOT_FOUND,
                SecurityErrorCodes.USER_NOT_FOUND,
                userId
            ));

        Set<Role> roles = new java.util.HashSet<>();
        for (String roleName : roleNames) {
            Role role = roleRepo.findByRoleName(roleName)
                .orElseThrow(() -> new LocalizedException(
                    Status.NOT_FOUND,
                    SecurityErrorCodes.ROLE_NOT_FOUND,
                    roleName
                ));
            roles.add(role);
        }

        user.setRoles(roles);
        UserAccount saved = repo.save(user);
        return ServiceResult.success(UserMapper.toDto(saved), Status.UPDATED);
    }

    /**
     * الحصول على أسماء أدوار المستخدم (Role Names Only)
     *
     * Returns list of role names for a user without exposing Role entities.
     * This method is preferred for API responses to avoid entity exposure.
     *
     * @param userId معرف المستخدم
     * @return قائمة أسماء الأدوار
     */
    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority(T(com.example.security.constants.SecurityPermissions).USER_VIEW)")
    public ServiceResult<List<String>> getUserRoleNames(Long userId) {
        UserAccount user = repo.findById(userId)
            .orElseThrow(() -> new LocalizedException(
                Status.NOT_FOUND,
                SecurityErrorCodes.USER_NOT_FOUND,
                userId
            ));

        return ServiceResult.success(user.getRoles().stream()
            .map(Role::getName)
            .toList());
    }

    /**
     * Dynamic search for users with filtering, sorting, and pagination
     * @param request SearchRequest containing filters, page, size, sortBy, sortDir
     * @return Page of UserDto matching the search criteria
     */
    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority(T(com.example.security.constants.SecurityPermissions).USER_VIEW)")
    public ServiceResult<Page<UserDto>> searchUsers(SearchRequest request) {
        // Build JPA Specification from filters
        Specification<UserAccount> spec = SpecBuilder.build(
            request,
            new SetAllowedFields(ALLOWED_USER_SEARCH_FIELDS),
            DefaultFieldValueConverter.INSTANCE
        );

        // Build Pageable with validated sort fields
        Pageable pageable = com.erp.common.search.PageableBuilder.from(
            request,
            ALLOWED_USER_SORT_FIELDS
        );

        // Execute query with specification and pageable
        Page<UserAccount> users = (spec != null) ? repo.findAll(spec, pageable) : repo.findAll(pageable);

        // Map to DTOs while session is open
        return ServiceResult.success(users.map(UserMapper::toDto));
    }

    /**
     * Delete user after checking for child relationships
     *
     * Business Prevention:
     * - Cannot delete user with active refresh tokens
     * - Pre-check via countByUser_Id() query
     *
     * @param userId User ID to delete
     * @throws LocalizedException if user has active refresh tokens or other child relationships
     */
    @Transactional
    @PreAuthorize("hasAuthority(T(com.example.security.constants.SecurityPermissions).USER_DELETE)")
    @CacheEvict(cacheNames = {"users", "userRoles"}, allEntries = true)
    public void deleteUser(Long userId) {
        // Find user
        UserAccount user = repo.findById(userId)
            .orElseThrow(() -> new LocalizedException(
                Status.NOT_FOUND,
                SecurityErrorCodes.USER_NOT_FOUND,
                userId
            ));

        // Business Prevention: Check for child relationships - refresh tokens
        long refreshTokenCount = refreshTokenRepo.countByUser_Id(userId);
        if (refreshTokenCount > 0) {
            throw new LocalizedException(
                Status.CONFLICT,
                SecurityErrorCodes.USER_HAS_ACTIVE_REFRESH_TOKENS,
                userId,
                refreshTokenCount
            );
        }

        repo.delete(user);
    }

    /**
     * Update user information
     * @param userId User ID to update
     * @param req Update request containing optional username, password, enabled status, and roles
     * @return Updated UserDto
     */
    @Transactional
    @PreAuthorize("hasAuthority(T(com.example.security.constants.SecurityPermissions).USER_UPDATE)")
    @CacheEvict(cacheNames = {"users", "userRoles"}, allEntries = true)
    public ServiceResult<UserDto> updateUser(Long userId, UpdateUserRequest req) {
        // Find user
        UserAccount user = repo.findById(userId)
            .orElseThrow(() -> new LocalizedException(
                Status.NOT_FOUND,
                SecurityErrorCodes.USER_NOT_FOUND,
                userId
            ));

        // Update username if provided
        if (req.username() != null && !req.username().isBlank()) {
            String newUsername = req.username().trim();
            // Check if username is already taken by another user
            if (!user.getUsername().equalsIgnoreCase(newUsername) &&
                repo.existsByUsernameIgnoreCase(newUsername)) {
                throw new LocalizedException(
                    Status.ALREADY_EXISTS,
                    SecurityErrorCodes.USERNAME_ALREADY_EXISTS,
                    newUsername
                );
            }
            user.setUsername(newUsername);
        }

        // Update password if provided
        if (req.password() != null && !req.password().isBlank()) {
            user.setPassword(encoder.encode(req.password()));
        }

        // Update enabled status if provided
        if (req.enabled() != null) {
            user.setEnabled(req.enabled());
        }

        // Update roles if provided
        if (req.roleNames() != null && !req.roleNames().isEmpty()) {
            Set<Role> roles = new java.util.HashSet<>();
            for (String roleName : req.roleNames()) {
                Role role = roleRepo.findByRoleName(roleName)
                    .orElseThrow(() -> new LocalizedException(
                        Status.NOT_FOUND,
                        SecurityErrorCodes.ROLE_NOT_FOUND,
                        roleName
                    ));
                roles.add(role);
            }
            user.setRoles(roles);
        }

        UserAccount saved = repo.save(user);
        return ServiceResult.success(UserMapper.toDto(saved), Status.UPDATED);
    }
}
