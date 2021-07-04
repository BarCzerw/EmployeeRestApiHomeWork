package com.sda.testing.repository;

import com.sda.testing.model.Employee;
import com.sda.testing.model.EmployeeLevel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    List<Employee> findAllByLevel(EmployeeLevel level);
}
