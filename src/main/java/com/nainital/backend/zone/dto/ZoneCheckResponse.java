package com.nainital.backend.zone.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ZoneCheckResponse {
    private boolean inZone;
    private String zoneName;
    private String etaLabel;
    private String message;
}
