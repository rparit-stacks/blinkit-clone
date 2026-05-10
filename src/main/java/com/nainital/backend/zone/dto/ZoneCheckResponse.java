package com.nainital.backend.zone.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ZoneCheckResponse {
    private boolean inZone;
    private String zoneName;
    private String etaLabel;
    private String message;
    private int deliveryFee;
    private int minOrderForFree;
    private double taxRate;
}
