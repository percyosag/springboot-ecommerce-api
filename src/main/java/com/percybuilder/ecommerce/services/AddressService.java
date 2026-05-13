package com.percybuilder.ecommerce.services;

import com.percybuilder.ecommerce.dtos.AddressRequest;
import com.percybuilder.ecommerce.dtos.AddressResponse;

import java.util.List;

public interface AddressService {

    AddressResponse createAddress(String username, AddressRequest addressRequest);

    List<AddressResponse> getUserAddresses(String username);

    AddressResponse getAddressById(String username, Long id);

    AddressResponse updateAddress(String username, Long id, AddressRequest addressRequest);

    void deleteAddress(String username, Long id);

    AddressResponse setDefaultAddress(String username, Long id);
}