package com.hacker.boooks.bean;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class AuthorProfile {
    private String name;
    private int noOfBooksWritten;
    private List<Book> booksWritten;
    private String mostWrittenGenre;
    private Book mostReadBook;
}
