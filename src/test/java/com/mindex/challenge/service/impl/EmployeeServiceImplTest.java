package com.mindex.challenge.service.impl;

import com.mindex.challenge.data.Compensation;
import com.mindex.challenge.data.Employee;
import com.mindex.challenge.data.ReportingStructure;
import com.mindex.challenge.service.EmployeeService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDate;
import java.time.Month;
import java.util.Arrays;
import java.util.Optional;

import static com.mindex.challenge.TestUtils.*;
import static com.mindex.challenge.controller.EmployeeControllerTest.validate;
import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class EmployeeServiceImplTest {

    @Autowired
    private EmployeeService employeeService;

    @Before
    public void setup() {
    }

    /**
     * Note! This test method was modified to call the EmployeeServiceImpl class directly so the methods are assured
     * to be tested.  The EmployeeController code could be modified to use an alternate implementation of
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
        Employee createdEmployee = employeeService.create(testEmployee);
        assertNotNull(createdEmployee.getEmployeeId());
        assertEmployeeEquivalence(testEmployee, createdEmployee);


        // Read checks
        Employee readEmployee = employeeService.read(createdEmployee.getEmployeeId());
        assertEquals(createdEmployee.getEmployeeId(), readEmployee.getEmployeeId());
        assertEmployeeEquivalence(createdEmployee, readEmployee);


        // Update checks
        readEmployee.setPosition("Development Manager");
        Employee updatedEmployee = employeeService.update(readEmployee);
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
        Employee multiLevelManager = employeeService.read(MULTI_LEVEL_MGR);
        ReportingStructure reportingStructure = employeeService.getReportingStructure(multiLevelManager);
        validate(reportingStructure, MULTI_LEVEL_MGR, 4);

        /*
         * Verify the result for an employee with just direct reports
         */
        Employee directOnlyManager = employeeService.read(DIRECT_ONLY_MGR);
        reportingStructure = employeeService.getReportingStructure(directOnlyManager);
        validate(reportingStructure, DIRECT_ONLY_MGR, 2);

        /*
         * Verify the result for an employee with no direct reports
         */
        Employee noReportsManager = employeeService.read(NO_REPORTS_MGR);
        reportingStructure = employeeService.getReportingStructure(noReportsManager);
        validate(reportingStructure, NO_REPORTS_MGR, 0);
    }

    @Test
    public void testGetReportingStructure_emptyAndUnpersistedEmployee() {
        Employee employee = new Employee();
        employee.setEmployeeId("11235813");

        ReportingStructure reportingStructure = employeeService.getReportingStructure(employee);
        assertNotNull(reportingStructure);
        validate(reportingStructure, employee.getEmployeeId(), 0);

        /*
         * Add two empty and unpersisted employees as direct reports
         */

        Employee reporting1 = new Employee();
        reporting1.setEmployeeId("23581321");
        Employee reporting2 = new Employee();
        reporting1.setEmployeeId("58132134");
        employee.setDirectReports(Arrays.asList(reporting1, reporting2));

        reportingStructure = employeeService.getReportingStructure(employee);
        assertNotNull(reportingStructure);
        validate(reportingStructure, employee.getEmployeeId(), 2);
    }

    /**
     * Note that the test relies on the data loaded from compensation_database.json so changes to that file have the
     * potential to break these tests.
     */
    @Test
    public void testGetCompensation() {

        /*
         * Make sure the correct Compensation record is returned when the employee has one record
         */
        Employee employee = new Employee();
        employee.setEmployeeId(STARR_EMPLOYEE_ID);

        Optional<Compensation> compensation = employeeService.getCompensation(employee);
        compensation.ifPresentOrElse(comp -> validate(comp, STARR_EMPLOYEE_ID, 1.0, LocalDate.of(2024, Month.NOVEMBER, 17)),
                                     () -> fail("Expected Compensation record"));

        /*
         * Make sure the correct Compensation record is returned when the Employee has more than one record
         */
        employee.setEmployeeId(BEST_EMPLOYEE_ID);
        compensation = employeeService.getCompensation(employee);
        compensation.ifPresentOrElse(comp -> validate(comp, BEST_EMPLOYEE_ID, 12000.0, LocalDate.of(1966, Month.DECEMBER, 5)),
                                     () -> fail("Expected Compensation record"));

        /*
         * Make sure the correct response is received when there is no Compensation record for the employee
         */
        employee.setEmployeeId(LENNON_EMPLOYEE_ID);
        compensation = employeeService.getCompensation(employee);
        assertTrue(compensation.isEmpty());
    }

    @Test
    public void testGetCompensation_invalidEmployeeId() {
        Employee employee = new Employee();
        employee.setEmployeeId("123");

        Optional<Compensation> compensation = employeeService.getCompensation(employee);
        assertTrue(compensation.isEmpty());
    }

    @Test
    public void testAddCompensation() {
        Employee employee = new Employee();
        employee.setEmployeeId(MCCARTNEY_EMPLOYEE_ID);

        Compensation compensation = employeeService.addCompensation(employee, 100.0, LocalDate.now());
        validate(compensation, MCCARTNEY_EMPLOYEE_ID, 100.0, LocalDate.now());
    }

    @Test(expected = RuntimeException.class)
    public void testAddCompensation_invalidEmployeeId() {
        Employee employee = new Employee();
        employee.setEmployeeId("123");

        employeeService.addCompensation(employee, 100.0, LocalDate.now());
    }
}
