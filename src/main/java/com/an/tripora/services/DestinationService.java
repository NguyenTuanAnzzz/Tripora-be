package com.an.tripora.services;

import com.an.tripora.dto.response.GetAllDestinationsResponse;
import com.an.tripora.models.Destination;
import com.an.tripora.repositories.DestinationRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class DestinationService {

    @Autowired
    private DestinationRepo repo;

    public Page<GetAllDestinationsResponse> getAllDestinations(int page, int size, String keyword) {
        Pageable pageable = PageRequest.of(page, size);

        Page<Destination> destinations;
        if (keyword != null && !keyword.trim().isEmpty()) {
            destinations = repo.findByNameContainingIgnoreCase(keyword.trim(), pageable);
        } else {
            destinations = repo.findAll(pageable);
        }

        return destinations.map(destination -> {
            GetAllDestinationsResponse response = new GetAllDestinationsResponse();

            response.setId(destination.getId());
            response.setName(destination.getName());
            response.setDescription(destination.getDescription());
            response.setImageUrl(destination.getImageUrl());

            return response;
        });
    }
}
