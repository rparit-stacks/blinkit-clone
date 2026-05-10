package com.nainital.backend.wallet.model;

public enum TransactionType {
    CREDIT,          // money added
    DEBIT,           // money deducted
    REFUND,          // refund to customer
    WITHDRAWAL,      // seller/user withdrawal
    SETTLEMENT,      // platform settles earnings to seller
    ORDER_EARNING,   // seller earns from an order
    ORDER_PAYMENT,   // customer pays for order
    COMMISSION,      // platform takes commission
    CASHBACK,        // future: cashback for customer
    MANUAL_CREDIT,   // admin manually adds
    MANUAL_DEBIT     // admin manually deducts
}
