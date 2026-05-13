package com.percybuilder.ecommerce.dtos;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class OrderShippingAddressResponse {

    private String fullName;
    private String phoneNumber;
    private String addressLine1;
    private String addressLine2;
    private String city;
    private String stateOrProvince;
    private String postalCode;
    private String country;
}