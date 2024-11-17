package com.mindex.challenge.service;

import com.mindex.challenge.data.Employee;
import com.mindex.challenge.data.ReportingStructure;
import com.mindex.challenge.data.Compensation;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface EmployeeService {
    Employee create(Employee employee);
    Employee read(String id);
    Employee update(Employee employee);

    /**
     * Generates a reporting structure record for an employee.  This record is always generated and not persisted.
     * @param employee the employee
     * @return a newly instantiated ReportingStructure record
     */
    ReportingStructure getReportingStructure(Employee employee);

    /**
     * Returns the employee's current Compensation if found.
     */
    Optional<Compensation> getCompensation(Employee employee);

    /**
     * Creates and persists a new Compensation record for an employee
     *
     * @param employee the employee
     * @param salary the amount the employee is being paid
     * @param effectiveDate the starting date for the salary amount
     *
     * @return a persisted Compensation record with a unique ID assigned to it
     */
    Compensation addCompensation(Employee employee, Double salary, LocalDate effectiveDate);
}
