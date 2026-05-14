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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

@RestController
@RequestMapping("/api/addresses")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Addresses", description = "Authenticated user shipping address management")
public class AddressController {

    private final AddressService addressService;

    public AddressController(AddressService addressService) {
        this.addressService = addressService;
    }
    @Operation(summary = "Create a shipping address")
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
    @Operation(summary = "Get current user's addresses")
    @GetMapping
    public ResponseEntity<List<AddressResponse>> getUserAddresses(
            Authentication authentication
    ) {
        return ResponseEntity.ok(
                addressService.getUserAddresses(authentication.getName())
        );
    }
    @Operation(summary = "Get address by ID")
    @GetMapping("/{id}")
    public ResponseEntity<AddressResponse> getAddressById(
            Authentication authentication,
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                addressService.getAddressById(authentication.getName(), id)
        );
    }
    @Operation(summary = "Update address")
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
    @Operation(summary = "Set address as default")
    @PatchMapping("/{id}/default")
    public ResponseEntity<AddressResponse> setDefaultAddress(
            Authentication authentication,
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                addressService.setDefaultAddress(authentication.getName(), id)
        );
    }
    @Operation(summary = "Delete address")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAddress(
            Authentication authentication,
            @PathVariable Long id
    ) {
        addressService.deleteAddress(authentication.getName(), id);
        return ResponseEntity.noContent().build();
    }
}