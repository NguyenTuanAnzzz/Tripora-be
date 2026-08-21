package com.an.tripora.controllers;

import com.an.tripora.dto.request.CreateDestinationRequest;
import com.an.tripora.dto.response.CreateDestinationResponse;
import com.an.tripora.dto.response.GetAllDestinationsResponse;
import com.an.tripora.services.DestinationService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
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
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "false") boolean all
    ) {
        return service.getAllDestinations(page, size, keyword, all);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping(
            value = "/create",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public CreateDestinationResponse createDestination(
            @Valid @ModelAttribute CreateDestinationRequest request
    ) {
        return service.createDestination(request);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public org.springframework.http.ResponseEntity<?> deleteDestination(@PathVariable Long id) {
        service.deleteDestination(id);
        return org.springframework.http.ResponseEntity.ok(
                java.util.Collections.singletonMap("message", "Đã khóa điểm đến thành công")
        );
    }
}