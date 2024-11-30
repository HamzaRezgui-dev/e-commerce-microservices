package com.hamza.ecommerce.order;

import com.hamza.ecommerce.product.PurchaseRequest;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.List;

public record OrderRequest(
    Integer id,
    String reference,
    @Positive(message = "Amount must be positive")
    BigDecimal amount,
    @NotNull(message = "Payment method is required")
    PaymentMethod paymentMethod,
    @NotNull(message = "Customer Should be present")
    @NotEmpty(message = "Customer Should be present")
    @NotBlank(message = "Customer Should be present")
    String customerId,
    @NotEmpty(message = "Products Should be present")
    List<PurchaseRequest> products
) {
}
