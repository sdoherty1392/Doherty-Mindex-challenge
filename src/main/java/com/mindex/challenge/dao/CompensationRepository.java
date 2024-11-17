package com.mindex.challenge.dao;

import com.mindex.challenge.data.Compensation;
import com.mindex.challenge.data.Employee;
import org.springframework.stereotype.Repository;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

@Repository
public interface CompensationRepository extends MongoRepository<Compensation, String> {
    /**
     * Returns the List of Compensation records for an employee in descending order so the most recent compensation
     * record is first in the list.
     *
     * @param employeeId the ID of the employee to retrieve compensation for
     * @return sorted List of Compensation records; empty List if no records are found
     */
    List<Compensation> findByEmployeeIdOrderByEffectiveDateDesc(String employeeId);
}
