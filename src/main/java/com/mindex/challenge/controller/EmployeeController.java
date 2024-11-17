package com.mindex.challenge.controller;

import com.mindex.challenge.data.Compensation;
import com.mindex.challenge.data.Employee;
import com.mindex.challenge.data.ReportingStructure;
import com.mindex.challenge.service.EmployeeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

@RestController
public class EmployeeController {
    private static final Logger LOG = LoggerFactory.getLogger(EmployeeController.class);

    @Autowired
    private EmployeeService employeeService;

    @PostMapping("/employee")
    public Employee create(@RequestBody Employee employee) {
        LOG.debug("Received employee create request for [{}]", employee);

        return employeeService.create(employee);
    }

    /**
     * Endpoint to retrieve the Employee
     *
     * HTTP 200 if employee is retrieved
     * HTTP 404 if employee is not found
     *
     * @param id the ID for the employee
     */
    @GetMapping("/employee/{id}")
    public Employee read(@PathVariable String id) {
        LOG.debug("Received employee create request for id [{}]", id);

        try {
            return employeeService.read(id);
        } catch (RuntimeException rtex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, rtex.getMessage());
        }
    }

    @PutMapping("/employee/{id}")
    public Employee update(@PathVariable String id, @RequestBody Employee employee) {
        LOG.debug("Received employee create request for id [{}] and employee [{}]", id, employee);

        employee.setEmployeeId(id);
        return employeeService.update(employee);
    }

    /**
     * Endpoint to retrieve the reporting structure record for an employee
     *
     * HTTP 200 if creporting record for the employee is retrieved
     * HTTP 404 if employee is not found
     *
     * @param id the ID for the employee
     */
    @GetMapping("/employee/{id}/reportingstructure")
    public ReportingStructure reportingStructure(@PathVariable String id) {
        LOG.debug("Received employee reporting structure request for id [{}]", id);

        Employee employee = read(id);
        return employeeService.getReportingStructure(employee);
    }

    /**
     * Endpoint to add a new Compensation record for the employee.
     *
     * Note that the Employee and ID do not have to be supplied in the Compensation body sent to the URL.  They will be
     * ignored if provided.
     *
     * @param id the Employee ID
     * @param compensation the Compensation record for the employee
     * @return the Compensation record or nothing if no Compensation records are found
     */
    @PostMapping("employee/{id}/compensation")
    public Compensation addCompensation(@PathVariable String id, @RequestBody Compensation compensation) {
        LOG.debug("Received request to add compensation for id [{}]", id);

        Employee employee = read(id);
        return employeeService.addCompensation(employee, compensation.salary(), compensation.effectiveDate());
    }

    /**
     * Endpoint to retrieve the current compensation for the Employee
     *
     * HTTP 200 if compensation for employee is retrieved
     * HTTP 404 if employee is not found or no compensation record is found
     *
     * @param id the Employee ID
     * @return the Compensation record
     */
    @GetMapping("employee/{id}/compensation")
    public Compensation getCompensation(@PathVariable String id) {
        LOG.debug("Received request to retrieve compensation for id [{}]", id);

        Employee employee = read(id);
        Optional<Compensation> compensation = employeeService.getCompensation(employee);

        if (compensation.isPresent()) {
            return compensation.get();
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No compensation found");
        }
    }
}
