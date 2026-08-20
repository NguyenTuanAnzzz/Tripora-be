package com.an.tripora.repositories;

import com.an.tripora.enums.Role;
import com.an.tripora.enums.UserStatus;
import com.an.tripora.models.User;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepo extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    Page<User> findByNameContainingIgnoreCase(String trim, Pageable pageable);

    boolean existsByPhone(String phone);

    boolean existsByPhoneAndIdNot(String phone, Long id);

    @Query("""
    SELECT u FROM User u
    WHERE
        (:keyword IS NULL OR :keyword = '' OR
         LOWER(u.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
         LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
         u.phone LIKE CONCAT('%', :keyword, '%'))
    AND
        (:role IS NULL OR u.role = :role)
    AND
        (:status IS NULL OR u.status = :status)
""")
    Page<User> searchUsers(
            @Param("keyword") String keyword,
            @Param("role") Role role,
            @Param("status") UserStatus status,
            Pageable pageable
    );
}