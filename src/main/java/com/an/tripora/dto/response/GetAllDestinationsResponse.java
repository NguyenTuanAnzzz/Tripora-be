package com.an.tripora.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class GetAllDestinationsResponse {

    private Long id;
    private String name;
    private String description;
    private List<String> imageUrls;
    private com.an.tripora.enums.Status status;
}