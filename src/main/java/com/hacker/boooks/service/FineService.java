package com.hacker.boooks.service;

import com.hacker.boooks.bean.Fine;
import org.springframework.http.ResponseEntity;

/**
 * Log management service.
 */
public interface FineService {

    /**
     * Retrieves the current fine details from the system.
     *
     * @return ResponseEntity containing the Fine details
     */
    ResponseEntity<Fine> getFineDetails();

    /**
     * Sets the number of days after which fine should be collected.
     *
     * @param days The number of days for fine collection
     * @return ResponseEntity indicating the success of the operation
     */
    ResponseEntity<String> setDaysForFine(int days);

    /**
     * Sets the amount to be collected as fine.
     *
     * @param amount The amount to be set as fine
     * @return ResponseEntity indicating the success of the operation
     */
    ResponseEntity<String> setAmountForFine(float amount);

}
