package com.sda.testing.service;

import com.sda.testing.exception.InvalidOperation;
import com.sda.testing.model.Employee;
import com.sda.testing.model.EmployeeLevel;
import com.sda.testing.repository.EmployeeRepository;
import com.sda.testing.repository.TeamRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

@SpringBootTest
@ActiveProfiles("tests")
class EmployeeServiceTests {
    private final EmployeeService employeeService;
    private final EmployeeRepository employeeRepository;

    @Autowired
    public EmployeeServiceTests(EmployeeRepository employeeRepository,
                                TeamRepository teamRepository) {
        this.employeeService = new EmployeeService(employeeRepository, teamRepository);
        this.employeeRepository = employeeRepository;
    }

    @Test
    void employeeListIsInitializedEmptyTest() {
        assert_initiallyEmpty();
    }

    @Nested
    class EmployeeManagementTests {
        private final String TEST_EMPLOYEE_FIRST_NAME = "Marian";
        private final String TEST_EMPLOYEE_LAST_NAME = "Kowalski";
        private final Double TEST_EMPLOYEE_SALARY = 2000.0;

        public EmployeeManagementTests() {
        }

        @BeforeEach
        void setupTest() {
            clearDatabase();
            assert_initiallyEmpty();
            employeeRepository.save(Employee.builder()
                    .firstName(TEST_EMPLOYEE_FIRST_NAME)
                    .lastName(TEST_EMPLOYEE_LAST_NAME)
                    .salary(TEST_EMPLOYEE_SALARY)
                    .level(EmployeeLevel.WORKER)
                    .build());
        }

        private void clearDatabase() {
            employeeRepository.deleteAll();
        }

        @Test
        void employeeCanBeAddedTest() {
            List<Employee> list = employeeService.findAll();
            Assertions.assertEquals(1, list.size());
        }

        @Test
        void addedEmployeeHasCorrectInformationTest() {
            List<Employee> list = employeeService.findAll();
            Assertions.assertEquals(1, list.size());

            Employee employee = list.get(0);
            Assertions.assertEquals(TEST_EMPLOYEE_FIRST_NAME, employee.getFirstName());
            Assertions.assertEquals(TEST_EMPLOYEE_LAST_NAME, employee.getLastName());
            Assertions.assertEquals(TEST_EMPLOYEE_SALARY, employee.getSalary());
        }

        @Test
        void addedEmployeeCanBeDeleted() {
            List<Employee> list = employeeService.findAll();
            Employee employee = list.get(0);
            long employeeId = employee.getId();
            employeeRepository.delete(employee);
            Assertions.assertFalse(employeeRepository.findById(employeeId).isPresent());
        }
    }

    private void assert_initiallyEmpty() {
        List<Employee> list = employeeService.findAll();
        Assertions.assertEquals(0, list.size());
    }

    @Nested
    class EmployeeSalaryManagementTests {
        private final Employee[] EMPLOYEES_INFO = new Employee[]{
                Employee.builder().firstName("Jan").lastName("Kowalski").salary(500.0).level(EmployeeLevel.WORKER).build(),
                Employee.builder().firstName("Kasia").lastName("Nowak").salary(2500.0).level(EmployeeLevel.WORKER).build(),
                Employee.builder().firstName("Iza").lastName("Le??niak").salary(5000.0).level(EmployeeLevel.MANAGER).build(),
        };

        @BeforeEach
        void setup() {
            employeeRepository.deleteAll();

            for (Employee employee : EMPLOYEES_INFO) {
                employeeRepository.save(employee);
            }
        }

        @Test
        void cantGiveRaiseWithoutEmployeeIdTest() {
            Exception e = Assertions.assertThrows(InvalidOperation.class, () -> {
                employeeService.giveRaise(9999999L, 20.0);
            });
            Assertions.assertNotNull(e);
        }

        @Test
        void canGiveRaiseToValidEmployee() {
            final double SALARY_RAISE_PERCENT = 6.0;
            final String EMPLOYEE_FIRST_NAME = "Jan";
            final String EMPLOYEE_LAST_NAME = "Kowalski";
            final long EMPLOYEE_ID = 1L;

            double initialSalary = getCurrentSalary(EMPLOYEE_FIRST_NAME, EMPLOYEE_LAST_NAME);

            try {
                employeeService.giveRaise(EMPLOYEE_ID,SALARY_RAISE_PERCENT);
            } catch (InvalidOperation invalidOperation) {
                invalidOperation.printStackTrace();
            }
            double raisedSalary = getCurrentSalary(EMPLOYEE_FIRST_NAME, EMPLOYEE_LAST_NAME);
            Assertions.assertEquals(raisedSalary,initialSalary*(1+(SALARY_RAISE_PERCENT/100)));
        }

        private double getCurrentSalary(String firstName, String lastName) {
            Employee employee = employeeRepository.findByFirstNameAndLastName(firstName, lastName);
            return employee.getSalary();
        }

    }
}