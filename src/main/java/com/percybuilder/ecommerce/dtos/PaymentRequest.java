package com.percybuilder.ecommerce.dtos;

import com.percybuilder.ecommerce.models.PaymentMethod;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PaymentRequest {

    @NotNull(message = "Payment method is required")
    @Schema(example = "TEST_PAYMENT")
    private PaymentMethod paymentMethod;

    @NotBlank(message = "Payment transaction id is required")
    @Size(max = 150, message = "Payment transaction id must not exceed 150 characters")
    @Schema(example = "TEST-TXN-1001")
    private String paymentTransactionId;
}