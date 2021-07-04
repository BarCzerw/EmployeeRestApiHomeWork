package com.sda.testing.service;

import com.sda.testing.exception.InvalidOperation;
import com.sda.testing.model.Employee;
import com.sda.testing.model.EmployeeLevel;
import com.sda.testing.repository.EmployeeRepository;
import com.sda.testing.repository.TeamRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class EmployeeService {
    private final EmployeeRepository employeeRepository;
    private final TeamRepository teamRepository;

    /**
     * List all employees.
     *
     * @return list of employees.
     */
    public List<Employee> findAll() {
        return employeeRepository.findAll();
    }

    /**
     * List all employees who are of given employee type.
     *
     * @param level - type, can be null, then all employees should be returned.
     * @return list of employees with same EmployeeLevel as provided in parameter.
     */
    public List<Employee> findAllFrom(EmployeeLevel level) {
        Optional<EmployeeLevel> levelOptional = Optional.ofNullable(level);
        if (levelOptional.isPresent()) {
            return employeeRepository.findAllByLevel(level);
        } else {
            return findAll();
        }
    }

    /**
     * Find employees by salary.
     *
     * @param salaryFrom - lower bound of salary. Can be null, then should be ignored.
     * @param salaryTo   - upper bound of salary. Can be null, then should be ignored.
     * @return list of employees which salary is between #salaryFrom and #salaryTo
     */
    public List<Employee> findAllBySalary(Double salaryFrom, Double salaryTo) {

        Optional<Double> lowerBoundOptional = Optional.ofNullable(salaryFrom);
        Optional<Double> upperBoundOptional = Optional.ofNullable(salaryTo);

        double lowerBound = lowerBoundOptional.orElse(0d);
        double upperBound = upperBoundOptional.orElse(0d);

        return findAll().stream().filter(employee ->
                employee.getSalary() > lowerBound && employee.getSalary() < upperBound
        ).collect(Collectors.toList());

    }

    /**
     * Give raise to employee found by Id.
     *
     * @param employeeId         - employee identifier, can't be null.
     * @param salaryRaisePercent - percentage of salary raise. Value can't be lower than -5 and higher than 100.
     * @throws InvalidOperation - if values of percentage or employee id is not provided, exception will be thrown.
     */
    public void giveRaise(Long employeeId, double salaryRaisePercent) throws InvalidOperation {
        if (employeeId == null || salaryRaisePercent < -5 || salaryRaisePercent > 100) {
            throw new InvalidOperation();
        } else {
            Optional<Employee> employeeOptional = employeeRepository.findById(employeeId);
            if (employeeOptional.isPresent()) {
                Employee employee = employeeOptional.get();
                employee.setSalary(employee.getSalary() * (1 + (salaryRaisePercent / 100)));
                employeeRepository.save(employee);
            } else {
                throw new InvalidOperation();
            }
        }

    }

    /**
     * Promote employee. Allowed promotions are:
     * - WORKER -> LEAD
     * - LEAD -> MANAGER
     * - MANAGER -> EXECUTIVE
     * - SALES -> MANAGER
     * - ACCOUNTING -> MANAGER
     * <p>
     * Independent employee cannot be promoted. Each promotion results in 5% net raise.
     * Promotion of manager to executive results in 3% raise.
     *
     * @param employeeId - identifier of promoted employee.
     * @throws InvalidOperation - if operation should not succeed, exception will be thrown.
     */
    public void givePromotion(Long employeeId) throws InvalidOperation {
        Optional<Employee> employeeOptional = employeeRepository.findById(employeeId);
        if (employeeOptional.isPresent()) {
            Employee employee = employeeOptional.get();

            switch (employee.getLevel()) {
                case WORKER:
                    employee.setLevel(EmployeeLevel.LEAD);
                    giveRaise(employeeId, 5);
                    break;
                case LEAD:
                case SALES:
                case ACCOUNTING:
                    employee.setLevel(EmployeeLevel.MANAGER);
                    giveRaise(employeeId, 5);
                    break;
                case MANAGER:
                    employee.setLevel(EmployeeLevel.EXECUTIVE);
                    giveRaise(employeeId, 3);
                    break;
                case EXECUTIVE:
                case INDEPENDENT:
                    throw new InvalidOperation();
            }

        }
    }
}
