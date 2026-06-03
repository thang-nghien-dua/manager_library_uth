package com.hacker.boooks.bean;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Log {
    private int logId;
    private int bookId;
    private int memberId;
    private LocalDate issueDate;
    private LocalDate returnDate;
    private float fine;
}
