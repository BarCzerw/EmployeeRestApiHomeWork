package com.sda.testing.controller;

import com.sda.testing.exception.InvalidOperation;
import com.sda.testing.model.Employee;
import com.sda.testing.model.EmployeeLevel;
import com.sda.testing.model.ResponseMessage;
import com.sda.testing.repository.EmployeeRepository;
import com.sda.testing.service.CompanyService;
import com.sda.testing.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
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
    public ResponseEntity<ResponseMessage<List<Employee>>> getAllEmployees() {
        return ResponseEntity.ok(new ResponseMessage<>(employeeService.findAll(), "Response OK!"));
    }

    @GetMapping("/level/{level}")
    public ResponseEntity<ResponseMessage<List<Employee>>> getAllEmployeesByLevel(@PathVariable(required = false) EmployeeLevel level) {
        return ResponseEntity.ok(new ResponseMessage<>(employeeService.findAllFrom(level), "Response OK!"));
    }

    @GetMapping("/salary")
    public ResponseEntity<ResponseMessage<List<Employee>>> getAllEmployeesBySalary(
            @RequestParam(required = false) Double salaryFrom,
            @RequestParam(required = false) Double salaryTo)
    {
        return ResponseEntity.ok(new ResponseMessage<>(employeeService.findAllBySalary(salaryFrom, salaryTo), "Respone OK!"));
    }

    @PostMapping("/salaryRaise")
    public ResponseEntity<ResponseMessage> giveRaise(long employeeId, double percentRaise) {
        try {
            employeeService.giveRaise(employeeId, percentRaise);
            return ResponseEntity.status(HttpStatus.OK).build();
        } catch (InvalidOperation invalidOperation) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @PostMapping("/promote")
    public ResponseEntity<ResponseMessage> promoteEmployee(long employeeId) {
        try {
            employeeService.givePromotion(employeeId);
            return ResponseEntity.status(HttpStatus.OK).build();
        } catch (InvalidOperation invalidOperation) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

}
