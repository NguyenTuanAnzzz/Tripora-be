package com.an.tripora.controllers;

import com.an.tripora.dto.response.GetAllDestinationsResponse;
import com.an.tripora.services.DestinationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/destinations")
public class DestinationController {

    @Autowired
    private DestinationService service;

    @GetMapping
    public Page<GetAllDestinationsResponse> getAllDestinations(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "9") int size,
            @RequestParam(required = false) String keyword
    ) {
        return service.getAllDestinations(page, size, keyword);
    }
}