package com.nainital.backend.user.dto;

import lombok.Data;

@Data
public class UpdateProfileRequest {

    private String name;
    private String phone;
    private String profileImage;
    private String gender;
    private String dateOfBirth;
}
