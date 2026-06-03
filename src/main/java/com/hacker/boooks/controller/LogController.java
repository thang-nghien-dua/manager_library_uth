package com.hacker.boooks.controller;

import com.hacker.boooks.bean.Log;
import com.hacker.boooks.service.LogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/logs")
@Tag(name = "Log Management", description = "APIs for managing logs")
@CrossOrigin(origins = "*")
@Slf4j
@SuppressWarnings("unused")
public class LogController {

    @Autowired
    private LogService logService;

    @Operation(summary = "Get all book logs", description = "This API allows you to retrieve a list of all book logs in the library. It returns information such as the log ID, book ID, member ID, issue date, return date, and fine amount (if applicable).")
    @GetMapping("")
    public ResponseEntity<List<Log>> getBooks() {
        return logService.getLogs();
    }

}
