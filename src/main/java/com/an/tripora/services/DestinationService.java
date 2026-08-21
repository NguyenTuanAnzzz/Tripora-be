package com.an.tripora.services;

import com.an.tripora.enums.Status;
import com.an.tripora.dto.request.CreateDestinationRequest;
import com.an.tripora.dto.response.CreateDestinationResponse;
import com.an.tripora.dto.response.GetAllDestinationsResponse;
import com.an.tripora.exceptions.BadRequestException;
import com.an.tripora.models.Destination;
import com.an.tripora.models.DestinationImage;
import com.an.tripora.repositories.DestinationRepo;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
public class DestinationService {

    @Autowired
    private DestinationRepo repo;

    @Autowired
    private CloudinaryService cloudinaryService;

    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public Page<GetAllDestinationsResponse> getAllDestinations(
            int page,
            int size,
            String keyword,
            boolean all
    ) {

        Pageable pageable = PageRequest.of(page, size);

        Page<Destination> destinations;

        if (keyword != null && !keyword.trim().isEmpty()) {
            if (all) {
                destinations = repo.findByNameContainingIgnoreCase(
                        keyword.trim(),
                        pageable
                );
            } else {
                destinations = repo.findByNameContainingIgnoreCaseAndStatus(
                        keyword.trim(),
                        Status.ACTIVE,
                        pageable
                );
            }
        } else {
            if (all) {
                destinations = repo.findAll(pageable);
            } else {
                destinations = repo.findByStatus(
                        Status.ACTIVE,
                        pageable
                );
            }
        }

        return destinations.map(destination -> {

            GetAllDestinationsResponse response =
                    new GetAllDestinationsResponse();

            response.setId(destination.getId());
            response.setName(destination.getName());
            response.setDescription(destination.getDescription());

            List<String> imageUrls = destination.getImages()
                    .stream()
                    .map(DestinationImage::getImageUrl)
                    .toList();

            response.setImageUrls(imageUrls);
            response.setStatus(destination.getStatus());

            return response;
        });
    }

    public CreateDestinationResponse createDestination(
            CreateDestinationRequest request
    ) {

        if (repo.existsByNameIgnoreCase(request.getName().trim())) {
            throw new BadRequestException(
                    "Destination với tên này đã tồn tại"
            );
        }

        if (request.getImages() == null || request.getImages().isEmpty()) {
            throw new BadRequestException(
                    "Destination phải có ít nhất một ảnh"
            );
        }

        Destination destination = new Destination();

        destination.setName(request.getName().trim());
        destination.setDescription(request.getDescription());

        int displayOrder = 1;

        for (MultipartFile image : request.getImages()) {

            java.util.Map<String, Object> uploadResult = cloudinaryService.uploadImage(image);

            DestinationImage destinationImage =
                    new DestinationImage();

            destinationImage.setImageUrl(uploadResult.get("secure_url").toString());
            destinationImage.setPublicId(uploadResult.get("public_id").toString());
            destinationImage.setDisplayOrder(displayOrder);
            destinationImage.setDestination(destination);

            destination.getImages().add(destinationImage);

            displayOrder++;
        }

        repo.save(destination);

        CreateDestinationResponse response =
                new CreateDestinationResponse();

        response.setMessage("Tạo destination thành công");

        return response;
    }

    public void deleteDestination(Long id) {
        Destination destination = repo.findById(id).orElseThrow(() -> 
                new BadRequestException("Không tìm thấy điểm đến"));
        
        destination.setStatus(com.an.tripora.enums.Status.INACTIVE);
        repo.save(destination);
    }
}
