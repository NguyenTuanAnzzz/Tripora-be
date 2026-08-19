package com.an.tripora.services;

import com.an.tripora.dto.response.GetAllVehiclesResponse;
import com.an.tripora.models.Vehicle;
import com.an.tripora.repositories.UserRepo;
import com.an.tripora.repositories.VehicleRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
public class VehicleService {

    @Autowired
    private VehicleRepo repo;

    public List<GetAllVehiclesResponse> getAllVehicles() {

        List<Vehicle> vehicles = repo.findAll();

        return vehicles.stream().map(vehicle -> {
            GetAllVehiclesResponse response = new GetAllVehiclesResponse();
            response.setId(vehicle.getId());
            response.setName(vehicle.getName());
            response.setDescription(vehicle.getDescription());
            return response;
        }).toList();
    }




}
