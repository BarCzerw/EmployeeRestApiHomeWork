package com.sda.testing.controller;

import com.sda.testing.model.Employee;
import com.sda.testing.model.EmployeeLevel;
import com.sda.testing.model.ResponseMessage;
import com.sda.testing.repository.EmployeeRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("tests")
public class EmployeeIntegrationTests {
    private final EmployeeRepository employeeRepository;
    private final TestRestTemplate testRestTemplate;

    @LocalServerPort
    int randomPort;

    @Autowired
    public EmployeeIntegrationTests(EmployeeRepository employeeRepository,
                                    TestRestTemplate testRestTemplate) {
        this.employeeRepository = employeeRepository;
        this.testRestTemplate = testRestTemplate;
    }

    @Nested
    class EmployeeSalaryManagementTests {
        private final Employee[] EMPLOYEES_INFO = new Employee[]{
                Employee.builder().firstName("Jan").lastName("Kowalski").salary(500.0).level(EmployeeLevel.WORKER).build(),
                Employee.builder().firstName("Kasia").lastName("Nowak").salary(2500.0).level(EmployeeLevel.WORKER).build(),
                Employee.builder().firstName("Iza").lastName("Leśniak").salary(5000.0).level(EmployeeLevel.MANAGER).build(),
        };

        @BeforeEach
        void setup() {
            employeeRepository.deleteAll();

            for (Employee employee : EMPLOYEES_INFO) {
                employeeRepository.save(employee);
            }
        }

        @Test
        void canGetListOfEmployeesWithSalaryBetween499And501() {
            Map<String, String> params = new HashMap<>();
            params.put("from", "499");
            params.put("to", "501");
            ResponseEntity<ResponseMessage> responseEntity = testRestTemplate.getForEntity(
                    "http://localhost:" + randomPort + "/employee/salary?salaryFrom={from}&salaryTo={to}",
                    ResponseMessage.class, params);
            Assertions.assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
            ResponseMessage responseBody = responseEntity.getBody();
            List<Employee> employeeList = (List<Employee>) responseBody.getBody();
            Assertions.assertEquals(1, employeeList.size());
        }

        @Test
        void canGetListOfEmployeesWithSalaryTo2501() {
            Map<String, String> params = new HashMap<>();
            params.put("to", "2501");
            ResponseEntity<ResponseMessage> responseEntity = testRestTemplate.getForEntity(
                    "http://localhost:" + randomPort + "/employee/salary?salaryTo={to}",
                    ResponseMessage.class, params);
            Assertions.assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
            ResponseMessage responseBody = responseEntity.getBody();
            List<Employee> employeeList = (List<Employee>) responseBody.getBody();
            Assertions.assertEquals(2, employeeList.size());
        }

        @Test
        void canGetListOfEmployeesWithSalaryFrom801() {
            Map<String, String> params = new HashMap<>();
            params.put("from", "801");
            ResponseEntity<ResponseMessage> responseEntity = testRestTemplate.getForEntity(
                    "http://localhost:" + randomPort + "/employee/salary?salaryFrom={from}",
                    ResponseMessage.class, params);
            Assertions.assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
            ResponseMessage responseBody = responseEntity.getBody();
            List<Employee> employeeList = (List<Employee>) responseBody.getBody();
            Assertions.assertEquals(2, employeeList.size());
        }

        @Test
        void canGetListOfEmployeesWithSalaryWithoutParams() {
            Map<String, String> params = new HashMap<>();
            ResponseEntity<ResponseMessage> responseEntity = testRestTemplate.getForEntity(
                    "http://localhost:" + randomPort + "/employee/salary",
                    ResponseMessage.class, params);
            Assertions.assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
            ResponseMessage responseBody = responseEntity.getBody();
            List<Employee> employeeList = (List<Employee>) responseBody.getBody();
            Assertions.assertEquals(3, employeeList.size());
        }

        @Test
        void canGetListOfEmployeesOfSpecifiedLevel() {
            Map<String, String> params = new HashMap<>();
            params.put("level", "MANAGER");
            ResponseEntity<ResponseMessage> responseEntity = testRestTemplate.getForEntity(
                    "http://localhost:" + randomPort + "/employee/level" + "?level={level}",
                    ResponseMessage.class, params);
            Assertions.assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
            ResponseMessage<List<Employee>> responseBody = responseEntity.getBody();
            List<Employee> employeeList = responseBody.getBody();
            Assertions.assertEquals(1, employeeList.size());
        }

        @Test
        void canGetListOfAllEmployeesWithoutSpecifiedLevel() {
            Map<String, String> params = new HashMap<>();
            ResponseEntity<ResponseMessage> responseEntity = testRestTemplate.getForEntity(
                    "http://localhost:" + randomPort + "/employee/level",
                    ResponseMessage.class, params);
            Assertions.assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
            ResponseMessage<List<Employee>> responseBody = responseEntity.getBody();
            List<Employee> employeeList = responseBody.getBody();
            Assertions.assertEquals(3, employeeList.size());
        }

    }

    @Nested
    class EmployeeSalaryRaiseManagementTests {
        private final Employee EMPLOYEES_INFO =
                Employee.builder().firstName("Jan").lastName("Kowalski").salary(500.0).level(EmployeeLevel.WORKER).build();
        private Employee SAVED_EMPLOYEE;

        void refreshSavedEmployee() {
            employeeRepository.findById(SAVED_EMPLOYEE.getId()).ifPresent(employee -> SAVED_EMPLOYEE = employee);
        }

        @BeforeEach
        void setup() {
            employeeRepository.deleteAll();
            SAVED_EMPLOYEE = employeeRepository.save(EMPLOYEES_INFO);
        }

        @Test
        void cannotGiveRaiseMinusTenPercent() {

            double PERCENT_RAISE = -10.0;
            Map<String, String> params = prepareParams(PERCENT_RAISE);

            ResponseEntity<ResponseMessage> responseEntity = getResponseMessage("/salaryRaise?employeeId={id}&percentRaise={raise}", params);

            Assertions.assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        }

        @Test
        void canGiveRaiseMinusFivePercent() {

            double PERCENT_RAISE = -5.0;
            Map<String, String> params = prepareParams(PERCENT_RAISE);

            ResponseEntity<ResponseMessage> responseEntity = getResponseMessage("/salaryRaise?employeeId={id}&percentRaise={raise}", params);

            Assertions.assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
            refreshSavedEmployee();
            assert_salaryRaise(PERCENT_RAISE);
        }

        @Test
        void canGiveRaiseTenPercent() {

            double PERCENT_RAISE = 10.0;
            Map<String, String> params = prepareParams(PERCENT_RAISE);

            ResponseEntity<ResponseMessage> responseEntity = getResponseMessage("/salaryRaise?employeeId={id}&percentRaise={raise}", params);

            Assertions.assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
            refreshSavedEmployee();
            assert_salaryRaise(PERCENT_RAISE);
        }

        @Test
        void canGiveRaiseHundredPercent() {

            double PERCENT_RAISE = 100.0;
            Map<String, String> params = prepareParams(PERCENT_RAISE);

            ResponseEntity<ResponseMessage> responseEntity = getResponseMessage("/salaryRaise?employeeId={id}&percentRaise={raise}", params);

            Assertions.assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
            refreshSavedEmployee();
            assert_salaryRaise(PERCENT_RAISE);
        }

        @Test
        void cannotGiveRaiseHundredOnePercent() {

            double PERCENT_RAISE = 101.0;
            Map<String, String> params = prepareParams(PERCENT_RAISE);

            ResponseEntity<ResponseMessage> responseEntity = getResponseMessage("/salaryRaise?employeeId={id}&percentRaise={raise}", params);

            Assertions.assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        }

        @Test
        void cannotGiveRaiseToInvalidEmployee() {

            double PERCENT_RAISE = 10.0;
            Map<String, String> params = prepareParams(PERCENT_RAISE);

            ResponseEntity<ResponseMessage> responseEntity = getResponseMessage("/salaryRaise?employeeId=99&percentRaise={raise}", params);

            Assertions.assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        }

        // --- HELP METHODS --- //

        private ResponseEntity<ResponseMessage> getResponseMessage(String url, Map<String, String> params) {
            ResponseEntity<ResponseMessage> responseEntity = testRestTemplate.getForEntity(
                    "http://localhost:" + randomPort + "/employee" + url,
                    ResponseMessage.class, params);
            return responseEntity;
        }

        private Map<String, String> prepareParams(double PERCENT_RAISE) {
            Map<String, String> params = new HashMap<>();
            params.put("id", SAVED_EMPLOYEE.getId().toString());
            params.put("raise", String.valueOf(PERCENT_RAISE));
            return params;
        }

        private void assert_salaryRaise(double PERCENT_RAISE) {
            Assertions.assertEquals(SAVED_EMPLOYEE.getSalary(), 500 * (1 + (PERCENT_RAISE / 100)));
        }

    }

    @Nested
    class EmployeePromotionManagementTests {
        private final Employee[] EMPLOYEES_INFO = {
                Employee.builder().firstName("Jan").lastName("Kowalski").salary(500.0).level(EmployeeLevel.WORKER).build(),
                Employee.builder().firstName("Adam").lastName("Matusiak").salary(1500.0).level(EmployeeLevel.ACCOUNTING).build(),
                Employee.builder().firstName("Zbigniew").lastName("Lech").salary(2500.0).level(EmployeeLevel.INDEPENDENT).build(),
                Employee.builder().firstName("Anna").lastName("Zalewska").salary(3500.0).level(EmployeeLevel.MANAGER).build()
        };

        @BeforeEach
        void setup() {
            employeeRepository.deleteAll();
            for (Employee employee : EMPLOYEES_INFO) {
                employeeRepository.save(employee);
            }
        }

        @Test
        void canPromoteWorkerToLead() {

            final String FIRST_NAME = "Jan";
            final String LAST_NAME = "Kowalski";

            long EMPLOYEE_ID = getEmployee(FIRST_NAME, LAST_NAME).getId();
            Map<String, String> params = new HashMap<>();
            ResponseEntity<ResponseMessage> responseEntity = getResponseEntity(params, EMPLOYEE_ID);

            Assertions.assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
            Employee employee = getEmployee(FIRST_NAME, LAST_NAME);
            Assertions.assertEquals(employee.getLevel(), EmployeeLevel.LEAD);

        }

        @Test
        void canPromoteAccountingToManager() {

            final String FIRST_NAME = "Adam";
            final String LAST_NAME = "Matusiak";

            long EMPLOYEE_ID = getEmployee(FIRST_NAME, LAST_NAME).getId();
            Map<String, String> params = new HashMap<>();
            ResponseEntity<ResponseMessage> responseEntity = getResponseEntity(params, EMPLOYEE_ID);

            Assertions.assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
            Employee employee = getEmployee(FIRST_NAME, LAST_NAME);
            Assertions.assertEquals(employee.getLevel(), EmployeeLevel.MANAGER);

        }

        @Test
        void canPromoteManagerToExecutive() {
            final String FIRST_NAME = "Anna";
            final String LAST_NAME = "Zalewska";

            long EMPLOYEE_ID = getEmployee(FIRST_NAME, LAST_NAME).getId();
            Map<String, String> params = new HashMap<>();
            ResponseEntity<ResponseMessage> responseEntity = getResponseEntity(params, EMPLOYEE_ID);

            Assertions.assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
            Employee employee = getEmployee(FIRST_NAME, LAST_NAME);
            Assertions.assertEquals(employee.getLevel(), EmployeeLevel.EXECUTIVE);

        }

        @Test
        void cannotPromoteIndependent() {

            final String FIRST_NAME = "Zbigniew";
            final String LAST_NAME = "Lech";

            long EMPLOYEE_ID = getEmployee(FIRST_NAME, LAST_NAME).getId();
            Map<String, String> params = new HashMap<>();
            ResponseEntity<ResponseMessage> responseEntity = getResponseEntity(params, EMPLOYEE_ID);

            Assertions.assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());

        }

        @Test
        void cannotPromoteInvalidEmployee() {

            Map<String, String> params = new HashMap<>();
            ResponseEntity<ResponseMessage> responseEntity = getResponseEntity(params, 99L);
            Assertions.assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());

        }

        private ResponseEntity<ResponseMessage> getResponseEntity(Map<String, String> params, Long id) {
            return testRestTemplate.exchange(
                    "http://localhost:" + randomPort + "/employee/promote",
                    HttpMethod.POST,
                    new HttpEntity<>(id),
                    ResponseMessage.class,
                    params);
        }

        private Employee getEmployee(String firstName, String lastName) {
            return employeeRepository.findByFirstNameAndLastName(firstName, lastName);
        }

    }

}
