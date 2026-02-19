package com.staffbase.employee_record_system.controller;

import com.razorpay.RazorpayException;
import com.staffbase.employee_record_system.dto.APIResponse;
import com.staffbase.employee_record_system.service.PaymentService;
import com.staffbase.employee_record_system.service.PdfInvoiceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;
    private final PdfInvoiceService pdfInvoiceService;

    @GetMapping("/invoice/{orderId}")
    public ResponseEntity<byte[]> downloadInvoice(@PathVariable String orderId) {
        return paymentService.getPaymentByOrderId(orderId)
                .map(payment -> {
                    byte[] pdf = pdfInvoiceService.generateInvoice(payment);
                    return ResponseEntity.ok()
                            .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION,
                                    "attachment; filename=invoice_" + orderId + ".pdf")
                            .contentType(org.springframework.http.MediaType.APPLICATION_PDF)
                            .body(pdf);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/user")
    public ResponseEntity<APIResponse<java.util.List<com.staffbase.employee_record_system.entity.Payment>>> getUserPayments(
            org.springframework.security.core.Authentication auth) {
        return ResponseEntity
                .ok(APIResponse.<java.util.List<com.staffbase.employee_record_system.entity.Payment>>builder()
                        .success(true)
                        .data(paymentService.getPaymentsByUser(auth.getName()))
                        .status(200)
                        .build());
    }

    @PostMapping("/create-order")
    public ResponseEntity<APIResponse<Map<String, Object>>> createOrder(@RequestBody Map<String, Object> data) {
        try {
            Double amount = Double.parseDouble(data.get("amount").toString());
            String planName = (String) data.getOrDefault("planName", "GROWTH");

            Map<String, Object> orderDetails = paymentService.createOrder(amount, planName);

            return ResponseEntity.ok(APIResponse.<Map<String, Object>>builder()
                    .success(true)
                    .message("Order created successfully")
                    .data(orderDetails)
                    .status(200)
                    .build());

        } catch (RazorpayException e) {
            return ResponseEntity.internalServerError().body(APIResponse.<Map<String, Object>>builder()
                    .success(false)
                    .message("Error creating Razorpay order: " + e.getMessage())
                    .status(500)
                    .build());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(APIResponse.<Map<String, Object>>builder()
                    .success(false)
                    .message("Internal server error: " + e.getMessage())
                    .status(500)
                    .build());
        }
    }

    @PostMapping("/verify-payment")
    public ResponseEntity<APIResponse<String>> verifyPayment(@RequestBody Map<String, String> data) {
        String orderId = data.get("razorpay_order_id");
        String paymentId = data.get("razorpay_payment_id");
        String signature = data.get("razorpay_signature");

        try {
            boolean isValid = paymentService.verifyPayment(orderId, paymentId, signature);

            if (isValid) {
                return ResponseEntity.ok(APIResponse.<String>builder()
                        .success(true)
                        .message("Payment verified successfully")
                        .data("Success")
                        .status(200)
                        .build());
            } else {
                return ResponseEntity.badRequest().body(APIResponse.<String>builder()
                        .success(false)
                        .message("Invalid payment signature")
                        .status(400)
                        .build());
            }
        } catch (RazorpayException e) {
            return ResponseEntity.internalServerError().body(APIResponse.<String>builder()
                    .success(false)
                    .message("Error verifying payment: " + e.getMessage())
                    .status(500)
                    .build());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(APIResponse.<String>builder()
                    .success(false)
                    .message("Internal server error: " + e.getMessage())
                    .status(500)
                    .build());
        }
    }

    @PostMapping("/webhook")
    public ResponseEntity<String> handleWebhook(
            @RequestBody String payload,
            @RequestHeader("x-razorpay-signature") String signature) {
        try {
            paymentService.handleWebhook(payload, signature);
            return ResponseEntity.ok("Webhook received");
        } catch (Exception e) {
            System.err.println("Webhook error: " + e.getMessage());
            return ResponseEntity.ok("Webhook processed with error");
        }
    }
}



