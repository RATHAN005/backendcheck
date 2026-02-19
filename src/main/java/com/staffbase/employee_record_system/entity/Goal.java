package com.staffbase.employee_record_system.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "performance_goals")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Goal {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    private String title;
    private String description;
    private LocalDate targetDate;

    @Enumerated(EnumType.STRING)
    private com.staffbase.employee_record_system.entity.GoalStatus status;

    private Integer weightage;
}



