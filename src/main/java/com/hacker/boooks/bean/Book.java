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
public class Book {
    private int bookId;
    private String title;
    private String author;
    private String genre;
    private LocalDate publication;
    private boolean isAvailable;
    private Integer holder;
}
