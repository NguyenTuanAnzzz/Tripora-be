package com.an.tripora.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Getter
@Setter
public class CreateDestinationRequest {

    @NotBlank
    private String name;

    private String description;

    private List<MultipartFile> images;
}