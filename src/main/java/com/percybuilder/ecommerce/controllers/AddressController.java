package com.percybuilder.ecommerce.controllers;

import com.percybuilder.ecommerce.dtos.AddressRequest;
import com.percybuilder.ecommerce.dtos.AddressResponse;
import com.percybuilder.ecommerce.services.AddressService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/addresses")
@SecurityRequirement(name = "bearerAuth")
public class AddressController {

    private final AddressService addressService;

    public AddressController(AddressService addressService) {
        this.addressService = addressService;
    }

    @PostMapping
    public ResponseEntity<AddressResponse> createAddress(
            Authentication authentication,
            @Valid @RequestBody AddressRequest addressRequest
    ) {
        AddressResponse createdAddress = addressService.createAddress(
                authentication.getName(),
                addressRequest
        );

        return new ResponseEntity<>(createdAddress, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<AddressResponse>> getUserAddresses(
            Authentication authentication
    ) {
        return ResponseEntity.ok(
                addressService.getUserAddresses(authentication.getName())
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<AddressResponse> getAddressById(
            Authentication authentication,
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                addressService.getAddressById(authentication.getName(), id)
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<AddressResponse> updateAddress(
            Authentication authentication,
            @PathVariable Long id,
            @Valid @RequestBody AddressRequest addressRequest
    ) {
        return ResponseEntity.ok(
                addressService.updateAddress(
                        authentication.getName(),
                        id,
                        addressRequest
                )
        );
    }

    @PatchMapping("/{id}/default")
    public ResponseEntity<AddressResponse> setDefaultAddress(
            Authentication authentication,
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                addressService.setDefaultAddress(authentication.getName(), id)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAddress(
            Authentication authentication,
            @PathVariable Long id
    ) {
        addressService.deleteAddress(authentication.getName(), id);
        return ResponseEntity.noContent().build();
    }
}