package com.staffbase.employee_record_system.service;

import com.staffbase.employee_record_system.dto.EmployeeRequest;
import com.staffbase.employee_record_system.dto.EmployeeResponse;
import com.staffbase.employee_record_system.entity.*;
import com.staffbase.employee_record_system.repository.DepartmentRepository;
import com.staffbase.employee_record_system.repository.EmployeeRepository;
import com.staffbase.employee_record_system.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@org.springframework.transaction.annotation.Transactional
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final com.staffbase.employee_record_system.repository.LeaveBalanceRepository leaveBalanceRepository;
    private final AuditLogService auditLogService;

    public EmployeeResponse createEmployee(EmployeeRequest request) {

        String currentUserEmail = org.springframework.security.core.context.SecurityContextHolder.getContext()
                .getAuthentication().getName();
        User currentUser = userRepository.findByEmail(currentUserEmail).orElse(null);

        if (currentUser != null && currentUser.getRole() != com.staffbase.employee_record_system.entity.Role.ADMIN) {
            long currentEmployeeCount = employeeRepository.count();
            com.staffbase.employee_record_system.entity.SubscriptionPlan plan = currentUser.getPlan();

            if (plan == com.staffbase.employee_record_system.entity.SubscriptionPlan.STARTER
                    && currentEmployeeCount >= 10) {
                throw new RuntimeException("Starter plan limit reached (10 employees). Please upgrade to Growth.");
            }
            if (plan == com.staffbase.employee_record_system.entity.SubscriptionPlan.GROWTH
                    && currentEmployeeCount >= 100) {
                throw new RuntimeException("Growth plan limit reached (100 employees). Please upgrade to Enterprise.");
            }

            if (plan != com.staffbase.employee_record_system.entity.SubscriptionPlan.STARTER &&
                    currentUser.getSubscriptionExpiry() != null &&
                    currentUser.getSubscriptionExpiry().isBefore(java.time.LocalDateTime.now())) {
                throw new RuntimeException("Your " + plan.name() + " subscription has expired. Please renew.");
            }
        }

        User user = userRepository.findByEmail(request.email()).orElse(null);

        String firstName = request.firstName();
        String lastName = request.lastName();

        if (request.name() != null && !request.name().isEmpty()) {
            if (request.name().contains(" ")) {
                String[] parts = request.name().split(" ", 2);
                firstName = parts[0];
                lastName = parts[1];
            } else {
                firstName = request.name();
                lastName = "";
            }
        }

        Department department = null;
        if (request.department() != null && !request.department().isEmpty()) {
            final String deptName = request.department();
            department = departmentRepository.findAll().stream()
                    .filter(d -> d.getName().equalsIgnoreCase(deptName))
                    .findFirst()
                    .orElseGet(() -> {
                        Department newDept = Department.builder()
                                .name(deptName)
                                .description("Auto-created department")
                                .build();
                        return departmentRepository.save(newDept);
                    });
        }

        Employee employee = Employee.builder()
                .firstName(firstName != null ? firstName : "")
                .lastName(lastName != null ? lastName : "")
                .jobTitle(request.jobTitle() != null ? request.jobTitle() : "Employee")
                .hireDate(request.hireDate() != null ? request.hireDate() : java.time.LocalDate.now())
                .dateOfBirth(request.dateOfBirth())
                .gender(request.gender() != null && !request.gender().isEmpty()
                        ? Gender.valueOf(request.gender().toUpperCase())
                        : null)
                .employmentType(request.employmentType() != null && !request.employmentType().isEmpty()
                        ? EmploymentType.valueOf(request.employmentType().toUpperCase())
                        : EmploymentType.FULL_TIME)
                .phoneNumber(request.phoneNumber())
                .personalEmail(request.personalEmail())
                .address(request.address())
                .nationality(request.nationality())
                .emergencyContactName(request.emergencyContactName())
                .emergencyContactPhone(request.emergencyContactPhone())
                .profilePictureUrl(request.profilePictureUrl())
                .user(user)
                .department(department)
                .manager(request.managerId() != null ? employeeRepository.findById(request.managerId()).orElse(null)
                        : null)
                .baseSalary(request.baseSalary() != null ? request.baseSalary() : 0.0)
                .build();

        Employee saved = employeeRepository.save(employee);

        initializeLeaveBalances(saved);

        auditLogService.logAction("CREATE_EMPLOYEE", currentUserEmail,
                "Employee Name: " + saved.getFirstName() + " " + saved.getLastName() + ", Dept: "
                        + (department != null ? department.getName() : "N/A"));

        return mapToResponse(saved);
    }

    private void initializeLeaveBalances(Employee employee) {
        for (LeaveType type : LeaveType.values()) {
            int entitled = switch (type) {
                case ANNUAL -> 20;
                case SICK -> 10;
                case CASUAL -> 8;
                case MATERNITY -> 90;
                case PATERNITY -> 14;
                case UNPAID -> 365;
                default -> 0;
            };
            LeaveBalance balance = LeaveBalance.builder()
                    .employee(employee)
                    .leaveType(type)
                    .totalEntitled(entitled)
                    .used(0)
                    .pending(0)
                    .build();
            leaveBalanceRepository.save(balance);
        }
    }

    public List<EmployeeResponse> getAllEmployees() {
        return employeeRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public com.staffbase.employee_record_system.dto.PageResponse<EmployeeResponse> getEmployees(
            int page, int size, String search, String department) {

        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size);

        String keywordParam = "%" + (search != null ? search : "") + "%";
        String deptParam = (department != null && !department.isEmpty()) ? "%" + department + "%" : null;

        org.springframework.data.domain.Page<Employee> employeePage = employeeRepository.searchEmployees(keywordParam,
                deptParam, pageable);

        List<EmployeeResponse> content = employeePage.getContent().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return new com.staffbase.employee_record_system.dto.PageResponse<>(
                content,
                employeePage.getNumber(),
                employeePage.getSize(),
                employeePage.getTotalElements(),
                employeePage.getTotalPages(),
                employeePage.isLast());
    }

    public EmployeeResponse getEmployeeById(java.util.UUID id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Employee not found with id: " + id));
        return mapToResponse(employee);
    }

    public EmployeeResponse updateEmployee(java.util.UUID id, EmployeeRequest request) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Employee not found with id: " + id));

        if (request.name() != null && !request.name().isEmpty()) {
            if (request.name().contains(" ")) {
                String[] parts = request.name().split(" ", 2);
                employee.setFirstName(parts[0]);
                employee.setLastName(parts[1]);
            } else {
                employee.setFirstName(request.name());
                employee.setLastName("");
            }
        } else if (request.firstName() != null) {
            employee.setFirstName(request.firstName());
            if (request.lastName() != null)
                employee.setLastName(request.lastName());
        }

        if (request.department() != null && !request.department().isEmpty()) {
            final String deptName = request.department();
            Department department = departmentRepository.findAll().stream()
                    .filter(d -> d.getName().equalsIgnoreCase(deptName))
                    .findFirst()
                    .orElseGet(() -> {
                        Department newDept = Department.builder()
                                .name(deptName)
                                .description("Auto-created department")
                                .build();
                        return departmentRepository.save(newDept);
                    });
            employee.setDepartment(department);
        }

        if (request.email() != null && !request.email().isEmpty()) {
            User user = userRepository.findByEmail(request.email()).orElse(null);
            if (user != null) {
                employee.setUser(user);
            }
        }

        if (request.jobTitle() != null)
            employee.setJobTitle(request.jobTitle());
        if (request.hireDate() != null)
            employee.setHireDate(request.hireDate());
        if (request.dateOfBirth() != null)
            employee.setDateOfBirth(request.dateOfBirth());
        if (request.gender() != null && !request.gender().isEmpty())
            employee.setGender(Gender.valueOf(request.gender().toUpperCase()));
        if (request.employmentType() != null && !request.employmentType().isEmpty())
            employee.setEmploymentType(EmploymentType.valueOf(request.employmentType().toUpperCase()));
        if (request.phoneNumber() != null)
            employee.setPhoneNumber(request.phoneNumber());
        if (request.personalEmail() != null)
            employee.setPersonalEmail(request.personalEmail());
        if (request.address() != null)
            employee.setAddress(request.address());
        if (request.nationality() != null)
            employee.setNationality(request.nationality());
        if (request.emergencyContactName() != null)
            employee.setEmergencyContactName(request.emergencyContactName());
        if (request.emergencyContactPhone() != null)
            employee.setEmergencyContactPhone(request.emergencyContactPhone());
        if (request.profilePictureUrl() != null)
            employee.setProfilePictureUrl(request.profilePictureUrl());

        if (request.managerId() != null) {
            employee.setManager(employeeRepository.findById(request.managerId()).orElse(null));
        }

        if (request.baseSalary() != null) {
            employee.setBaseSalary(request.baseSalary());
        }

        Employee updated = employeeRepository.save(employee);
        return mapToResponse(updated);
    }

    public void deleteEmployee(java.util.UUID id) {
        Employee emp = employeeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Employee not found with id: " + id));

        employeeRepository.delete(emp);

        String currentUser = org.springframework.security.core.context.SecurityContextHolder.getContext()
                .getAuthentication().getName();
        auditLogService.logAction("DELETE_EMPLOYEE", currentUser,
                "Employee ID: " + id + ", Name: " + emp.getFirstName() + " " + emp.getLastName());
    }

    private EmployeeResponse mapToResponse(Employee emp) {
        return new EmployeeResponse(
                emp.getId(),
                emp.getFirstName(),
                emp.getLastName(),
                emp.getUser() != null ? emp.getUser().getEmail() : "No User Linked",
                emp.getJobTitle(),
                emp.getDepartment() != null ? emp.getDepartment().getName() : "N/A",
                emp.getHireDate(),
                emp.getDateOfBirth(),
                emp.getGender(),
                emp.getEmploymentType(),
                emp.getPhoneNumber(),
                emp.getPersonalEmail(),
                emp.getAddress(),
                emp.getNationality(),
                emp.getEmergencyContactName(),
                emp.getEmergencyContactPhone(),
                emp.getProfilePictureUrl(),
                emp.getManager() != null ? (emp.getManager().getFirstName() + " " + emp.getManager().getLastName())
                        : "None",
                emp.getManager() != null ? emp.getManager().getId() : null,
                emp.getBaseSalary());
    }
}
