package com.an.tripora.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GetAllDestinationsResponse {

    private Long id;
    private String name;
    private String description;
    private String imageUrl;
}