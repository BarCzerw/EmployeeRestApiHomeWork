package com.sda.testing.controller;

import com.sda.testing.exception.InvalidOperation;
import com.sda.testing.model.Employee;
import com.sda.testing.model.EmployeeLevel;
import com.sda.testing.repository.EmployeeRepository;
import com.sda.testing.service.CompanyService;
import com.sda.testing.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/employee")
@RequiredArgsConstructor
public class EmployeeController {
    private final EmployeeService employeeService;

    // TODO: poniżej dodaj metody kontrolera pozwalające na:
    //  - listowanie pracowników
    //  - listowanie pracowników po poziomach
    //  - szukanie pracowników po pensjach
    //  - daj podwyżkę
    //  - daj awans (promotion) pracownikowi

    @GetMapping("/all")
    public List<Employee> getAllEmployees() {
        return employeeService.findAll();
    }

    @GetMapping("/level/{level}")
    public List<Employee> getAllEmployeesByLevel(@PathVariable(required = false) EmployeeLevel level) {
        return employeeService.findAllFrom(level);
    }

    @GetMapping("/salary")
    public List<Employee> getAllEmployeesBySalary(
            @RequestParam(required = false) Double salaryFrom,
            @RequestParam(required = false) Double salaryTo)
    {
            return employeeService.findAllBySalary(salaryFrom, salaryTo);
    }

}
