package com.staffbase.employee_record_system.service;

import com.staffbase.employee_record_system.dto.DepartmentDTO;
import com.staffbase.employee_record_system.entity.Department;
import com.staffbase.employee_record_system.exception.ResourceNotFoundException;
import com.staffbase.employee_record_system.repository.DepartmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DepartmentService {

    private final DepartmentRepository departmentRepository;

    public DepartmentDTO createDepartment(DepartmentDTO dto) {
        if (departmentRepository.existsByName(dto.getName())) {
            throw new RuntimeException("Department already exists with name: " + dto.getName());
        }

        Department department = Department.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .managerName(dto.getManagerName())
                .build();

        Department saved = departmentRepository.save(department);
        return mapToDTO(saved);
    }

    public List<DepartmentDTO> getAllDepartments() {
        return departmentRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public DepartmentDTO getDepartmentById(UUID id) {
        Department dept = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + id));
        return mapToDTO(dept);
    }

    public DepartmentDTO updateDepartment(UUID id, DepartmentDTO dto) {
        Department dept = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + id));

        dept.setName(dto.getName());
        dept.setDescription(dto.getDescription());
        dept.setManagerName(dto.getManagerName());

        Department saved = departmentRepository.save(dept);
        return mapToDTO(saved);
    }

    public void deleteDepartment(UUID id) {
        if (!departmentRepository.existsById(id)) {
            throw new ResourceNotFoundException("Department not found with id: " + id);
        }
        departmentRepository.deleteById(id);
    }

    private DepartmentDTO mapToDTO(Department dept) {
        return DepartmentDTO.builder()
                .id(dept.getId())
                .name(dept.getName())
                .description(dept.getDescription())
                .managerName(dept.getManagerName())
                .employeeCount(dept.getEmployees() != null ? dept.getEmployees().size() : 0)
                .build();
    }
}
