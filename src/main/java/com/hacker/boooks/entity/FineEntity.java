package com.hacker.boooks.entity;

import jakarta.persistence.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "fine")
public class FineEntity {
    @Id
    @Column(name = "sl_no")
    private Integer slNo;

    @Column(name = "days_overdue", nullable = false)
    private Integer daysOverdue;

    @Column(name = "fine_amount", nullable = false)
    private Float fineAmount;
}