package com.an.tripora.repositories;

import com.an.tripora.models.EmailVerification;
import com.an.tripora.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EmailVerificationRepo
        extends JpaRepository<EmailVerification, Long> {

    Optional<EmailVerification> findByUser(User user);

    Optional<EmailVerification> findByUser_Email(String email);
}