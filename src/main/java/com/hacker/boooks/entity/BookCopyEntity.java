package com.hacker.boooks.entity;

import jakarta.persistence.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "book_copy")
public class BookCopyEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "copy_id")
    private Integer copyId;

    @Column(name = "book_id", nullable = false)
    private Integer bookId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    public enum Status {
        AVAILABLE, BORROWED, LOST, MAINTENANCE
    }
}