package com.mindex.challenge.service.impl;

import com.mindex.challenge.dao.CompensationRepository;
import com.mindex.challenge.dao.EmployeeRepository;
import com.mindex.challenge.data.Compensation;
import com.mindex.challenge.data.Employee;
import com.mindex.challenge.data.ReportingStructure;
import com.mindex.challenge.service.EmployeeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.UUID;
import java.util.Collections;
import java.util.List;
import java.util.Optional;


@Service
public class EmployeeServiceImpl implements EmployeeService {

    private static final Logger LOG = LoggerFactory.getLogger(EmployeeServiceImpl.class);

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private CompensationRepository compensationRepository;

    @Override
    public Employee create(Employee employee) {
        LOG.debug("Creating employee [{}]", employee);

        employee.setEmployeeId(UUID.randomUUID().toString());
        employeeRepository.insert(employee);

        return employee;
    }

    @Override
    public Employee read(String id) {
        LOG.debug("Retrieving employee with id [{}]", id);

        Employee employee = employeeRepository.findByEmployeeId(id);

        if (employee == null) {
            throw new RuntimeException("Invalid employeeId: " + id);
        }

        return employee;
    }

    @Override
    public Employee update(Employee employee) {
        LOG.debug("Updating employee [{}]", employee);

        return employeeRepository.save(employee);
    }

    @Override
    public ReportingStructure getReportingStructure(final Employee employee) {
        int totalReports = getTotalReports(employee);
        return new ReportingStructure(employee, totalReports);
    }

    @Override
    public Optional<Compensation> getCompensation(Employee employee) {
        List<Compensation> compensationList = compensationRepository.findByEmployeeIdOrderByEffectiveDateDesc(employee.getEmployeeId());
        return (!compensationList.isEmpty()) ? Optional.of(compensationList.get(0)) : Optional.empty();
    }

    @Override
    public Compensation addCompensation(Employee employee, Double salary, LocalDate effectiveDate) {
        // Make sure that the employee exists in the database before proceeding (call will throw an exception)
        read(employee.getEmployeeId());

        Compensation compensation = new Compensation(UUID.randomUUID().toString(), employee.getEmployeeId(), salary, effectiveDate);
        compensationRepository.insert(compensation);
        return compensation;
    }

    /**
     * Determines the number employees with a direct and indirect reporting relationship with the provided employee.
     *
     * @param employee the Employee to get reports for
     *
     * @return number of reporting employees
     */
    private int getTotalReports(Employee employee) {
        // Make sure the employee is completely loaded
        employee = retrieveEmployeeIfNecessary(employee);

        // Get the list of direct reports for the employee
        List<Employee> directReports = Optional.ofNullable(employee.getDirectReports())
                                                .orElse(Collections.emptyList());

        int totalReports = directReports.size();    // begin the total with the number of direct reports

        // Recurse down the reporting hierarchy to add in the indirect reports
        for (Employee reportingEmployee : directReports) {
            totalReports += getTotalReports(reportingEmployee);
        }

        return totalReports;
    }

    /**
     * Makes sure that the supplied Employee has been defined completely and tries to load it from the database if not.
     * We need to be able to rely that on the fact that the list of direct reports for the employee is correct.
     */
    private Employee retrieveEmployeeIfNecessary(Employee employee) {
        if (isNotComplete(employee)) {
            // Load the employee from the database; if it can't be loaded then use the employee passed in
            try {
                employee = read(employee.getEmployeeId());
            } catch (RuntimeException ex) {
                /*
                 * This is OK -- the employee may not be persisted yet; at this point we can take the list of direct
                 * reports on faith
                 */
            }
        }

        return employee;
    }

    /**
     * Determines if the provided Employee object is complete by evaluating a subset of fields for null values.
     *
     * If the employeeId is not null and the other fields (except directReports) are null then the Employee probably
     * needs to be loaded from the database to be complete.
     *
     * @return true if the Employee does not seem to be complete; false if it does
     */
    private boolean isNotComplete(Employee employee) {
        return  (employee.getEmployeeId() != null)
                && (employee.getFirstName() == null)
                && (employee.getLastName() == null)
                && (employee.getPosition() == null)
                && (employee.getDepartment() == null);
    }
}
