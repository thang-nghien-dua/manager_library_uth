package com.hacker.boooks.controller;

import com.hacker.boooks.bean.Book;
import com.hacker.boooks.bean.Member;
import com.hacker.boooks.bean.MemberBO;
import com.hacker.boooks.bean.MemberProfile;
import com.hacker.boooks.service.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/members")
@Tag(name = "Member Management", description = "APIs for managing members")
@CrossOrigin(origins = "*")
@Slf4j
@SuppressWarnings("unused")
public class MemberController {

    @Autowired
    private MemberService memberService;

    @Operation(summary = "Get all members", description = "Retrieve a list of all members registered in the library.")
    @GetMapping("")
    public ResponseEntity<List<Member>> getMembers() {
        return memberService.getMembers();
    }

    @Operation(summary = "Get a member by ID", description = "Retrieve the information of a member based on their unique ID.")
    @GetMapping("/{memberId}")
    public ResponseEntity<Member> getMember(@PathVariable int memberId) {
        return memberService.getMember(memberId);
    }

    @Operation(summary = "Get member profile", description = "Retrieve the profile information of a member based on their unique ID.")
    @GetMapping("/{memberId}/profile")
    public ResponseEntity<MemberProfile> getMemberProfile(@PathVariable int memberId) {
        return memberService.getMemberProfile(memberId);
    }

    @Operation(summary = "Add a member", description = "Add a new member to the library database.")
    @PostMapping
    public ResponseEntity<String> addMember(@RequestParam String name, @RequestParam String email, @RequestParam String phoneNumber) {
        MemberBO memberBO = new MemberBO(name, email, phoneNumber);
        return memberService.addMember(memberBO);
    }

    @Operation(summary = "Update a member", description = "Update the information of a member based on their unique ID.")
    @PutMapping("/{memberId}")
    public ResponseEntity<String> updateMember(@PathVariable int memberId, @RequestParam String name, @RequestParam String email, @RequestParam String phoneNumber) {
        MemberBO memberBO = new MemberBO(name, email, phoneNumber);
        return memberService.updateMember(memberId, memberBO);
    }

    @Operation(summary = "Delete a member", description = "Delete a member from the library based on their unique ID.")
    @DeleteMapping("/{memberId}")
    public ResponseEntity<String> deleteMember(@PathVariable int memberId) {
        return memberService.deleteMember(memberId);
    }

    @Operation(summary = "Get books currently held by a member", description = "Retrieve a list of books currently held by a member in the library. It returns information such as book titles, authors, publication dates, availability, and the member's ID.")
    @GetMapping("/{memberId}/books")
    public ResponseEntity<List<Book>> getBooksForMember(@PathVariable int memberId) {
        return memberService.getBooksForMember(memberId);
    }

}
