package com.mindex.challenge;

import com.mindex.challenge.data.Employee;

import static org.junit.Assert.assertEquals;

public class TestUtils {

    public static final String BEST_EMPLOYEE_ID = "62c1084e-6e34-4630-93fd-9153afb65309";
    public static final String STARR_EMPLOYEE_ID = "03aa1462-ffa9-4978-901b-7c001562cf6f";
    public static final String LENNON_EMPLOYEE_ID = "16a596ae-edd3-4847-99fe-c4518e82c86f";
    public static final String MCCARTNEY_EMPLOYEE_ID = "b7839309-3348-463b-a7e3-5de1c168beb3";

    public static final String MULTI_LEVEL_MGR = LENNON_EMPLOYEE_ID;
    public static final String DIRECT_ONLY_MGR = STARR_EMPLOYEE_ID;
    public static final String NO_REPORTS_MGR = BEST_EMPLOYEE_ID;

    public static void assertEmployeeEquivalence(final Employee expected, final Employee actual) {
        assertEquals(expected.getFirstName(), actual.getFirstName());
        assertEquals(expected.getLastName(), actual.getLastName());
        assertEquals(expected.getDepartment(), actual.getDepartment());
        assertEquals(expected.getPosition(), actual.getPosition());
    }
}