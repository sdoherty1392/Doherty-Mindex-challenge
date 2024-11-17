package com.mindex.challenge.controller;

import com.mindex.challenge.data.Compensation;
import com.mindex.challenge.data.Employee;
import com.mindex.challenge.data.ReportingStructure;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDate;
import java.time.Month;
import java.util.Optional;

import static com.mindex.challenge.TestUtils.*;
import static org.junit.Assert.*;


@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class EmployeeControllerTest {
    private String employeeUrl;
    private String employeeIdUrl;
    private String reportingStructureUrl;
    private String compensationUrl;

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Before
    public void setup() {
        employeeUrl = "http://localhost:" + port + "/employee";
        employeeIdUrl = "http://localhost:" + port + "/employee/{id}";
        reportingStructureUrl = employeeIdUrl + "/reportingstructure";
        compensationUrl = employeeIdUrl + "/compensation";
    }

    /**
     * Note! This test method was moved from EmployeeServiceImpl since they may not actually test the
     * EmployeeServiceImpl.  The EmployeeController code could be modified to use an alternate implementation of
     * EmployeeService or a different class altogether.
     */
    @Test
    public void testCreateReadUpdate() {
        Employee testEmployee = new Employee();
        testEmployee.setFirstName("John");
        testEmployee.setLastName("Doe");
        testEmployee.setDepartment("Engineering");
        testEmployee.setPosition("Developer");

        // Create checks
        Employee createdEmployee = restTemplate.postForEntity(employeeUrl, testEmployee, Employee.class).getBody();

        assertNotNull(createdEmployee.getEmployeeId());
        assertEmployeeEquivalence(testEmployee, createdEmployee);


        // Read checks
        Employee readEmployee = restTemplate.getForEntity(employeeIdUrl, Employee.class, createdEmployee.getEmployeeId()).getBody();
        assertEquals(createdEmployee.getEmployeeId(), readEmployee.getEmployeeId());
        assertEmployeeEquivalence(createdEmployee, readEmployee);


        // Update checks
        readEmployee.setPosition("Development Manager");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Employee updatedEmployee =
                restTemplate.exchange(employeeIdUrl,
                        HttpMethod.PUT,
                        new HttpEntity<Employee>(readEmployee, headers),
                        Employee.class,
                        readEmployee.getEmployeeId()).getBody();

        assertEmployeeEquivalence(readEmployee, updatedEmployee);
    }

    /**
     * Tests the implementation of the getReportingStructure() method.
     *
     * Note that the tests rely on the data loaded from employee_database.json so changes to that file have the potential
     * to break these tests.
     */
    @Test
    public void testGetReportingStructure() {

        /*
         * Verify result for employee with multiple reporting levels (i.e., has direct reports with their own direct reports)
         */
        ResponseEntity<ReportingStructure> response = restTemplate.getForEntity(reportingStructureUrl, ReportingStructure.class, MULTI_LEVEL_MGR);
        Optional<ReportingStructure> reportingStructure = validateResponse(HttpStatus.OK, response);
        reportingStructure.ifPresentOrElse(rs -> validate(rs, MULTI_LEVEL_MGR, 4),
                                           () -> fail("Expected ReportingStructure record"));

        /*
         * Verify the result for an employee with just direct reports
         */
        response = restTemplate.getForEntity(reportingStructureUrl, ReportingStructure.class, DIRECT_ONLY_MGR);
        reportingStructure = validateResponse(HttpStatus.OK, response);
        reportingStructure.ifPresentOrElse(rs -> validate(rs, DIRECT_ONLY_MGR, 2),
                                           () -> fail("Expected ReportingStructure record"));

        /*
         * Verify the result for an employee with no direct reports
         */
        response = restTemplate.getForEntity(reportingStructureUrl, ReportingStructure.class, NO_REPORTS_MGR);
        reportingStructure = validateResponse(HttpStatus.OK, response);
        reportingStructure.ifPresentOrElse(rs -> validate(rs, NO_REPORTS_MGR, 0),
                                           () -> fail("Expected ReportingStructure record"));
    }

    @Test
    public void testGetReportingStructure_invalidEmployeeId() {
        ResponseEntity<ReportingStructure> response = restTemplate.getForEntity(reportingStructureUrl, ReportingStructure.class, "123");
        Optional<ReportingStructure> reportingStructure = validateResponse(HttpStatus.NOT_FOUND, response);
        assertTrue(reportingStructure.isEmpty());
    }

    /**
     * Tests the implementation of the getCompensation() method.
     *
     * Note that the tests rely on the data loaded from compensation_database.json so changes to that file have the
     * potential to break these tests.
     */
    @Test
    public void testGetCompensation() {
        /*
         * Make sure the correct Compensation record is returned when the employee has one record
         */
        ResponseEntity<Compensation> response = restTemplate.getForEntity(compensationUrl, Compensation.class, STARR_EMPLOYEE_ID);
        Optional<Compensation> compensation = validateResponse(HttpStatus.OK, response);
        compensation.ifPresentOrElse(comp -> validate(comp, STARR_EMPLOYEE_ID, 1.0, LocalDate.of(2024, Month.NOVEMBER, 17)),
                                     () -> fail("Expected Compensation record"));

        /*
         * Make sure the correct Compensation record is returned when the Employee has more than one record
         */
        response = restTemplate.getForEntity(compensationUrl, Compensation.class, BEST_EMPLOYEE_ID);
        compensation = validateResponse(HttpStatus.OK, response);
        compensation.ifPresentOrElse(comp -> validate(comp, BEST_EMPLOYEE_ID, 12000.0, LocalDate.of(1966, Month.DECEMBER, 5)),
                                     () -> fail("Expected Compensation record"));

        /*
         * Make sure the correct response is received when there is no Compensation record for the employee
         */
        response = restTemplate.getForEntity(compensationUrl, Compensation.class, LENNON_EMPLOYEE_ID);
        compensation = validateResponse(HttpStatus.NOT_FOUND, response);
        assertTrue(compensation.isEmpty());
    }

    @Test
    public void testGetCompensation_invalidEmployeeId() {
        ResponseEntity<Compensation> response = restTemplate.getForEntity(compensationUrl, Compensation.class, "123");
        Optional<Compensation> compensation = validateResponse(HttpStatus.NOT_FOUND, response);
        assertTrue(compensation.isEmpty());
    }

    @Test
    public void testAddCompensation() {
        String updatedUrl = compensationUrl.replace("{id}", MCCARTNEY_EMPLOYEE_ID);

        // Put an employee id in the Compensation record to ensure it's ignored
        Compensation newCompensation = new Compensation(null, "123", 100.0, LocalDate.now());

        ResponseEntity<Compensation> response = restTemplate.postForEntity(updatedUrl, newCompensation, Compensation.class);
        Optional<Compensation> compensation = validateResponse(HttpStatus.OK, response);
        compensation.ifPresentOrElse(comp -> {
                                                validate(comp, MCCARTNEY_EMPLOYEE_ID, newCompensation.salary(), newCompensation.effectiveDate());
                                                assertNotNull(comp.id());   // make sure an ID was assigned
                                             },
                                     () -> fail("Expected Compensation record"));

    }

    @Test
    public void testAddCompensation_invalidEmployeeId() {
        String updatedUrl = compensationUrl.replace("{id}", "123");

        // Put an employee id in the Compensation record to ensure it's ignored
        Compensation newCompensation = new Compensation(null, MCCARTNEY_EMPLOYEE_ID, 100.0, LocalDate.now());

        ResponseEntity<Compensation> response = restTemplate.postForEntity(updatedUrl, newCompensation, Compensation.class);
        Optional<Compensation> compensation = validateResponse(HttpStatus.NOT_FOUND, response);
        assertTrue(compensation.isEmpty());
    }

    private <T> Optional<T> validateResponse(HttpStatus expected, ResponseEntity<T> response) {
        assertNotNull(response);
        assertEquals(expected, response.getStatusCode());

        if (HttpStatus.OK.equals(expected)) {
            return Optional.ofNullable(response.getBody());
        } else {
            return Optional.empty();
        }
    }

    public static void validate(Compensation actual, String expectedEmployeeId, Double expectedSalary, LocalDate expectedEffectiveDate) {
        assertEquals(expectedEmployeeId, actual.employeeId());
        assertEquals(expectedSalary, actual.salary());
        assertEquals(expectedEffectiveDate, actual.effectiveDate());
    }

    public static void validate(ReportingStructure actual, String expectedEmployeeId, int expectedNumberOfReports) {
        assertNotNull(actual);
        assertEquals(expectedEmployeeId, actual.employee().getEmployeeId());
        assertEquals(expectedNumberOfReports, actual.numberOfReports());
    }
}
