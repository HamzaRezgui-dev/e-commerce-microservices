package com.hamza.ecommerce.order;

import com.hamza.ecommerce.customer.CustomerClient;
import com.hamza.ecommerce.exception.BusinessException;
import com.hamza.ecommerce.kafka.OrderConfirmation;
import com.hamza.ecommerce.kafka.OrderProducer;
import com.hamza.ecommerce.orderline.OrderLineRequest;
import com.hamza.ecommerce.orderline.OrderLineService;
import com.hamza.ecommerce.payment.PaymentClient;
import com.hamza.ecommerce.payment.PaymentRequest;
import com.hamza.ecommerce.product.ProductClient;
import com.hamza.ecommerce.product.PurchaseRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final CustomerClient customerClient;
    private final ProductClient productClient;
    private final OrderRepository orderRepository;
    private final OrderMapper mapper;
    private final OrderLineService orderLineService;
    private final OrderProducer orderProducer;
    private final PaymentClient paymentClient;
    public Integer createOrder(@Valid OrderRequest request) {
        var customer = customerClient.findCustomerById(request.customerId())
                .orElseThrow(() -> new BusinessException("Cannot create order:: No Customer exists with the provided ID"));

        var purchasedProducts = productClient.purchaseProducts(request.products());

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

        var paymentRequest = new PaymentRequest(
                request.amount(),
                request.paymentMethod(),
                order.getId(),
                order.getReference(),
                customer
        );
        paymentClient.requestOrderPayment(paymentRequest);

        orderProducer.sendOrderConfirmation(
                new OrderConfirmation(
                        order.getReference(),
                        order.getTotalAmount(),
                        order.getPaymentMethod(),
                        customer,
                        purchasedProducts

                )
        );

        return order.getId();
    }

    public List<OrderResponse> findAll() {
        return orderRepository.findAll().stream()
                .map(mapper::toOrderResponse)
                .toList();
    }

    public OrderResponse findById(Integer orderId) {
        return orderRepository.findById(orderId)
                .map(mapper::toOrderResponse)
                .orElseThrow(() -> new BusinessException("No order found with the provided ID"));
    }
}
