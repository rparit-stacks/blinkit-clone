package com.nainital.backend.zone.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ZoneCheckRequest {
    @NotNull
    private Double lat;
    @NotNull
    private Double lng;
}
