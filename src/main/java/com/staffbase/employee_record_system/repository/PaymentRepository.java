package com.staffbase.employee_record_system.repository;

import com.staffbase.employee_record_system.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface PaymentRepository extends JpaRepository<Payment, UUID> {
    Optional<Payment> findByOrderId(String orderId);

    java.util.List<Payment> findByUserEmail(String email);
}



