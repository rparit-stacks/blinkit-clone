package com.nainital.backend.wallet.dto;

import lombok.Data;

@Data
public class WithdrawalCreateRequest {
    private double amountRupees;
    private String bankAccountNumber;
    private String bankIfsc;
    private String bankAccountHolderName;
    private String bankName;
    private String upiId;
}
