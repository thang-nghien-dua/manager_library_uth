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
public class MemberProfile {
    private String name;
    private String email;
    private String phoneNumber;
    private List<Book> currentlyHolding;
}
