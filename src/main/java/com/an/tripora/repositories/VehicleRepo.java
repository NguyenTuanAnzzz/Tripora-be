package com.an.tripora.repositories;

import com.an.tripora.models.Destination;
import com.an.tripora.models.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VehicleRepo extends JpaRepository<Vehicle, Long> {
}
