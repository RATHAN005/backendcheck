package com.staffbase.employee_record_system.service;

import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.razorpay.Utils;
import com.staffbase.employee_record_system.entity.*;
import com.staffbase.employee_record_system.repository.PaymentRepository;
import com.staffbase.employee_record_system.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PaymentService {

    public Optional<Payment> getPaymentByOrderId(String orderId) {
        return paymentRepository.findByOrderId(orderId);
    }

    public java.util.List<Payment> getPaymentsByUser(String email) {
        return paymentRepository.findByUserEmail(email);
    }

    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;

    @Value("${razorpay.key.id}")
    private String razorpayKeyId;

    @Value("${razorpay.key.secret}")
    private String razorpayKeySecret;

    @Value("${razorpay.webhook.secret}")
    private String razorpayWebhookSecret;

    @Transactional
    public Map<String, Object> createOrder(Double amount, String planName) throws RazorpayException {

        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email).orElse(null);

        String orderId;
        int amountInPaise = (int) (amount * 100);

        if ("rzp_test_placeholder".equals(razorpayKeyId)) {
            orderId = "order_mock_" + System.currentTimeMillis();
        } else {
            RazorpayClient razorpayClient = new RazorpayClient(razorpayKeyId, razorpayKeySecret);
            JSONObject orderRequest = new JSONObject();
            orderRequest.put("amount", amountInPaise);
            orderRequest.put("currency", "INR");
            orderRequest.put("receipt", "txn_" + System.currentTimeMillis());
            Order order = razorpayClient.orders.create(orderRequest);
            orderId = order.get("id");
        }

        Payment payment = Payment.builder()
                .orderId(orderId)
                .amount(amount)
                .currency("INR")
                .status(PaymentStatus.PENDING)
                .user(user)
                .planName(planName)
                .build();

        if (payment != null) {
            paymentRepository.save(payment);
        }

        return Map.of(
                "orderId", orderId,
                "amount", amountInPaise,
                "currency", "INR",
                "keyId", razorpayKeyId);
    }

    @Transactional
    public boolean verifyPayment(String orderId, String paymentId, String signature) throws RazorpayException {
        boolean isValid = false;

        if ("rzp_test_placeholder".equals(razorpayKeyId)) {
            isValid = true;
        } else {
            JSONObject options = new JSONObject();
            options.put("razorpay_order_id", orderId);
            options.put("razorpay_payment_id", paymentId);
            options.put("razorpay_signature", signature);
            isValid = Utils.verifyPaymentSignature(options, razorpayKeySecret);
        }

        processPaymentUpdate(orderId, paymentId, signature, isValid);
        return isValid;
    }

    @Transactional
    public void handleWebhook(String payload, String signature) throws RazorpayException {

        if (!"placeholder_webhook_secret".equals(razorpayWebhookSecret)) {
            boolean isValid = Utils.verifyWebhookSignature(payload, signature, razorpayWebhookSecret);
            if (!isValid) {
                throw new RazorpayException("Invalid webhook signature");
            }
        }

        JSONObject json = new JSONObject(payload);
        String event = json.getString("event");

        if ("payment.captured".equals(event) || "order.paid".equals(event)) {
            JSONObject paymentEntity = json.getJSONObject("payload").getJSONObject("payment").getJSONObject("entity");
            String orderId = paymentEntity.getString("order_id");
            String paymentId = paymentEntity.getString("id");

            processPaymentUpdate(orderId, paymentId, null, true);
        }
    }

    @Transactional
    public void refundPayment(String orderId) throws RazorpayException {
        Optional<Payment> paymentOpt = paymentRepository.findByOrderId(orderId);
        if (paymentOpt.isPresent()) {
            Payment payment = paymentOpt.get();
            if (payment.getStatus() != PaymentStatus.SUCCESS) {
                throw new RuntimeException("Only successful payments can be refunded");
            }

            com.razorpay.RazorpayClient client = new com.razorpay.RazorpayClient(razorpayKeyId, razorpayKeySecret);
            org.json.JSONObject refundRequest = new org.json.JSONObject();
            refundRequest.put("payment_id", payment.getPaymentId());
            refundRequest.put("amount", (int) (payment.getAmount() * 100));

            client.refunds.create(refundRequest);

            payment.setStatus(PaymentStatus.REFUNDED);
            paymentRepository.save(payment);
        }
    }

    private void processPaymentUpdate(String orderId, String paymentId, String signature, boolean isValid) {
        Optional<Payment> paymentOpt = paymentRepository.findByOrderId(orderId);
        if (paymentOpt.isPresent()) {
            Payment payment = paymentOpt.get();
            if (payment.getStatus() == PaymentStatus.SUCCESS) {
                return;
            }

            payment.setPaymentId(paymentId);
            if (signature != null) {
                payment.setSignature(signature);
            }

            if (isValid) {
                payment.setStatus(PaymentStatus.SUCCESS);

                if (payment.getUser() != null) {
                    User user = payment.getUser();
                    try {
                        SubscriptionPlan plan = SubscriptionPlan.valueOf(payment.getPlanName().toUpperCase());
                        user.setPlan(plan);
                        user.setSubscriptionExpiry(java.time.LocalDateTime.now().plusDays(30));
                        userRepository.save(user);
                    } catch (IllegalArgumentException e) {
                    }
                }
            } else {
                payment.setStatus(PaymentStatus.FAILED);
            }
            paymentRepository.save(payment);
        }
    }
}
