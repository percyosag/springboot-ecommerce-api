package com.percybuilder.ecommerce.services;

import com.percybuilder.ecommerce.dtos.AddressRequest;
import com.percybuilder.ecommerce.dtos.AddressResponse;
import com.percybuilder.ecommerce.exceptions.ResourceNotFoundException;
import com.percybuilder.ecommerce.models.Address;
import com.percybuilder.ecommerce.models.AppUser;
import com.percybuilder.ecommerce.repositories.AddressRepository;
import com.percybuilder.ecommerce.repositories.AppUserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AddressServiceImpl implements AddressService {

    private final AddressRepository addressRepository;
    private final AppUserRepository appUserRepository;

    public AddressServiceImpl(
            AddressRepository addressRepository,
            AppUserRepository appUserRepository
    ) {
        this.addressRepository = addressRepository;
        this.appUserRepository = appUserRepository;
    }

    @Override
    @Transactional
    public AddressResponse createAddress(String username, AddressRequest addressRequest) {
        AppUser appUser = findUserByUsername(username);

        boolean userHasAddresses = addressRepository.existsByAppUserUsername(username);
        boolean shouldBeDefault = addressRequest.isDefaultAddress() || !userHasAddresses;

        if (shouldBeDefault) {
            clearDefaultAddress(username);
        }

        Address address = Address.builder()
                .appUser(appUser)
                .fullName(addressRequest.getFullName())
                .phoneNumber(addressRequest.getPhoneNumber())
                .addressLine1(addressRequest.getAddressLine1())
                .addressLine2(addressRequest.getAddressLine2())
                .city(addressRequest.getCity())
                .stateOrProvince(addressRequest.getStateOrProvince())
                .postalCode(addressRequest.getPostalCode())
                .country(addressRequest.getCountry())
                .defaultAddress(shouldBeDefault)
                .build();

        Address savedAddress = addressRepository.save(address);

        return toResponse(savedAddress);
    }

    @Override
    public List<AddressResponse> getUserAddresses(String username) {
        return addressRepository.findByAppUserUsername(username)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public AddressResponse getAddressById(String username, Long id) {
        Address address = findAddressByUsernameAndId(username, id);
        return toResponse(address);
    }

    @Override
    @Transactional
    public AddressResponse updateAddress(
            String username,
            Long id,
            AddressRequest addressRequest
    ) {
        Address address = findAddressByUsernameAndId(username, id);

        if (addressRequest.isDefaultAddress()) {
            clearDefaultAddress(username);
            address.setDefaultAddress(true);
        }

        address.setFullName(addressRequest.getFullName());
        address.setPhoneNumber(addressRequest.getPhoneNumber());
        address.setAddressLine1(addressRequest.getAddressLine1());
        address.setAddressLine2(addressRequest.getAddressLine2());
        address.setCity(addressRequest.getCity());
        address.setStateOrProvince(addressRequest.getStateOrProvince());
        address.setPostalCode(addressRequest.getPostalCode());
        address.setCountry(addressRequest.getCountry());

        Address updatedAddress = addressRepository.save(address);

        return toResponse(updatedAddress);
    }

    @Override
    @Transactional
    public void deleteAddress(String username, Long id) {
        Address address = findAddressByUsernameAndId(username, id);
        addressRepository.delete(address);
    }

    @Override
    @Transactional
    public AddressResponse setDefaultAddress(String username, Long id) {
        Address address = findAddressByUsernameAndId(username, id);

        clearDefaultAddress(username);

        address.setDefaultAddress(true);
        Address updatedAddress = addressRepository.save(address);

        return toResponse(updatedAddress);
    }

    private AppUser findUserByUsername(String username) {
        return appUserRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found with username: " + username
                ));
    }

    private Address findAddressByUsernameAndId(String username, Long id) {
        return addressRepository.findByAppUserUsernameAndId(username, id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Address not found with id: " + id
                ));
    }

    private void clearDefaultAddress(String username) {
        List<Address> addresses = addressRepository.findByAppUserUsername(username);

        addresses.forEach(address -> address.setDefaultAddress(false));

        addressRepository.saveAll(addresses);
    }

    private AddressResponse toResponse(Address address) {
        return AddressResponse.builder()
                .id(address.getId())
                .fullName(address.getFullName())
                .phoneNumber(address.getPhoneNumber())
                .addressLine1(address.getAddressLine1())
                .addressLine2(address.getAddressLine2())
                .city(address.getCity())
                .stateOrProvince(address.getStateOrProvince())
                .postalCode(address.getPostalCode())
                .country(address.getCountry())
                .defaultAddress(address.isDefaultAddress())
                .createdAt(address.getCreatedAt())
                .updatedAt(address.getUpdatedAt())
                .build();
    }
}