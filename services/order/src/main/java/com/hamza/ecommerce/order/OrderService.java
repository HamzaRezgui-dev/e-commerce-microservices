package com.hamza.ecommerce.order;

import com.hamza.ecommerce.customer.CustomerClient;
import com.hamza.ecommerce.exception.BusinessException;
import com.hamza.ecommerce.orderline.OrderLineRequest;
import com.hamza.ecommerce.orderline.OrderLineService;
import com.hamza.ecommerce.product.ProductClient;
import com.hamza.ecommerce.product.PurchaseRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final CustomerClient customerClient;
    private final ProductClient productClient;
    private final OrderRepository orderRepository;
    private final OrderMapper mapper;
    private final OrderLineService orderLineService;
    public Integer createOrder(@Valid OrderRequest request) {
        var customer = customerClient.findCustomerById(request.customerId())
                .orElseThrow(() -> new BusinessException("Cannot create order:: No Customer exists with the provided ID"));

        productClient.purchaseProducts(request.products());

        var order = orderRepository.save(mapper.toOrder(request));

        for (PurchaseRequest purchaseRequest: request.products()) {
            orderLineService.saveOrderLine(
                    new OrderLineRequest(
                            null,
                            order.getId(),
                            purchaseRequest.productId(),
                            purchaseRequest.quantity()
                    )
            );
        }
        return null;
    }
}
