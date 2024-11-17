package com.mindex.challenge.data;

/**
 * An immutable reporting structure record for an employee.
 *
 * @param employee the employee
 * @param numberOfReports number of people directly or indirectly reporting to the employee
 */
public record ReportingStructure(Employee employee, int numberOfReports) {
}