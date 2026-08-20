package com.an.tripora.repositories;

import com.an.tripora.models.Destination;
import com.an.tripora.models.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DestinationRepo extends JpaRepository<Destination, Long> {
    Page<Destination> findByNameContainingIgnoreCase(String keyword, Pageable pageable);
}
