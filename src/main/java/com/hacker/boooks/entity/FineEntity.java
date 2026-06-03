package com.hacker.boooks.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "fine")
public class FineEntity {
    @Id
    @Column(name = "sl_no", nullable = false)
    private Integer slNo;
    @Column(name = "days_overdue", nullable = false)
    private Integer daysOverdue;
    @Column(name = "fine_amount", nullable = false)
    private Float fineAmount;
}