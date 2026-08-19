package com.an.tripora.controllers;

import com.an.tripora.dto.response.GetAllVehiclesResponse;
import com.an.tripora.models.Vehicle;
import com.an.tripora.services.VehicleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/vehicles")
public class VehicleController {
    @Autowired
    private VehicleService service;

    @GetMapping("")
    public List<GetAllVehiclesResponse> getAllVehicles() {
        return service.getAllVehicles();
    }

}
