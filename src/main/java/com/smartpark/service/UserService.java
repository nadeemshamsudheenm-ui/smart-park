package com.smartpark.service;

import com.smartpark.dto.RegisterRequest;
import com.smartpark.dto.UserDto;
import com.smartpark.entity.Role;
import com.smartpark.entity.User;
import com.smartpark.exception.BadRequestException;
import com.smartpark.exception.ResourceNotFoundException;
import com.smartpark.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditLogService auditLogService;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, AuditLogService auditLogService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.auditLogService = auditLogService;
    }

    @Transactional
    public UserDto registerCustomer(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail().trim().toLowerCase())) {
            throw new BadRequestException("Email address is already registered: " + request.getEmail());
        }

        User user = new User();
        user.setFullName(request.getFullName().trim());
        user.setEmail(request.getEmail().trim().toLowerCase());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setPhone(request.getPhone() != null ? request.getPhone().trim() : null);
        user.setRole(Role.CUSTOMER); // Public registration is ALWAYS CUSTOMER
        user.setEnabled(true);

        User savedUser = userRepository.save(user);
        auditLogService.log(savedUser, "CUSTOMER_REGISTER", "New customer registered: " + savedUser.getEmail());

        return UserDto.fromEntity(savedUser);
    }

    @Transactional(readOnly = true)
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
    }

    @Transactional(readOnly = true)
    public List<UserDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(UserDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<UserDto> getUsersByRole(Role role) {
        return userRepository.findByRole(role).stream()
                .map(UserDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public UserDto updateUserStatus(Long id, boolean enabled) {
        User user = getUserById(id);
        user.setEnabled(enabled);
        User saved = userRepository.save(user);
        auditLogService.log(saved, "USER_STATUS_CHANGE", "User account enabled status changed to: " + enabled);
        return UserDto.fromEntity(saved);
    }

    @Transactional
    public UserDto updateUserRole(Long id, Role role) {
        User user = getUserById(id);
        user.setRole(role);
        User saved = userRepository.save(user);
        auditLogService.log(saved, "USER_ROLE_CHANGE", "User role changed to: " + role);
        return UserDto.fromEntity(saved);
    }

    @Transactional
    public UserDto updateProfile(Long id, String fullName, String phone) {
        User user = getUserById(id);
        if (fullName != null && !fullName.trim().isEmpty()) {
            user.setFullName(fullName.trim());
        }
        if (phone != null) {
            user.setPhone(phone.trim());
        }
        User saved = userRepository.save(user);
        auditLogService.log(saved, "USER_PROFILE_UPDATE", "User updated personal profile details");
        return UserDto.fromEntity(saved);
    }
}
