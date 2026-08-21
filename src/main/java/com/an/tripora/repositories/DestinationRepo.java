package com.an.tripora.repositories;

import com.an.tripora.enums.Status;
import com.an.tripora.models.Destination;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DestinationRepo extends JpaRepository<Destination, Long> {

    boolean existsByNameIgnoreCase(String name);

    Page<Destination> findByStatus(
            Status status,
            Pageable pageable
    );

    Page<Destination> findByNameContainingIgnoreCaseAndStatus(
            String name,
            Status status,
            Pageable pageable
    );

    Page<Destination> findByNameContainingIgnoreCase(
            String name,
            Pageable pageable
    );
}