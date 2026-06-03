package com.hacker.boooks.service;

import com.hacker.boooks.bean.Log;
import org.springframework.http.ResponseEntity;

import java.util.List;

/**
 * Log management service.
 */
public interface LogService {

    /**
     * Retrieves a list of all book logs.
     *
     * @return ResponseEntity with a list of book logs if available, or an empty list if no logs exist.
     */
    ResponseEntity<List<Log>> getLogs();

}
