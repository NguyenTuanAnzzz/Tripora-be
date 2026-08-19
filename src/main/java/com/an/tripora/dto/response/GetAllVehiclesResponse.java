package com.an.tripora.dto.response;


import com.an.tripora.enums.Role;
import jakarta.persistence.Column;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GetAllVehiclesResponse {
    private Long id;
    private String name;
    private String description;
}
