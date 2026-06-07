package com.hacker.boooks.entity;

import jakarta.persistence.*;
import lombok.*;

import java.sql.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "log")
public class LogEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "log_id")
    private Integer logId;

    @Column(name = "copy_id", nullable = false)
    private Integer copyId;

    @Column(name = "member_id", nullable = false)
    private Integer memberId;

    @Column(name = "issue_date", nullable = false)
    private Date issueDate;

    @Column(name = "due_date")
    private Date dueDate;

    @Column(name = "return_date")
    private Date returnDate;

    private Float fine;
}