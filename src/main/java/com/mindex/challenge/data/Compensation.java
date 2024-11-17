package com.mindex.challenge.data;

import java.time.LocalDate;

/**
 * An immutable compensation record for an employee.
 *
 * @param id Unique identifier for record
 * @param employeeId ID of the Employee who receives this compensation
 * @param salary The dollars being paid to the employee
 * @param effectiveDate The date that the employee starts receiving this salary
 */
public record Compensation(String id, String employeeId, Double salary, LocalDate effectiveDate) {
}