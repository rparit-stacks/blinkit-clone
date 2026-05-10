package com.nainital.backend.seller.dto;

import lombok.Data;

@Data
public class SellerLoginRequest {
    private String email;
    private String password;
}
