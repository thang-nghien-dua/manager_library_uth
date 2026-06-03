package com.hacker.boooks.service;

import com.hacker.boooks.bean.Book;
import com.hacker.boooks.bean.Member;
import com.hacker.boooks.bean.MemberBO;
import com.hacker.boooks.bean.MemberProfile;
import org.springframework.http.ResponseEntity;

import java.util.List;

/**
 * Member management service.
 */
public interface MemberService {

    /**
     * Get all members.
     *
     * @return ResponseEntity containing a list of all members
     */
    ResponseEntity<List<Member>> getMembers();

    /**
     * Get a member by ID.
     *
     * @param memberId the ID of the member to retrieve
     * @return ResponseEntity containing the member information if found, or not found response if not found
     */
    ResponseEntity<Member> getMember(int memberId);

    /**
     * Get the profile of a member.
     *
     * @param memberId the ID of the member
     * @return ResponseEntity containing the member profile if found, or not found response if not found
     */
    ResponseEntity<MemberProfile> getMemberProfile(int memberId);

    /**
     * Adds a new member to the library.
     *
     * @param memberBO The MemberBO object containing the member details to be added.
     * @return ResponseEntity with a success message if the member is added successfully, or an error response if an exception occurs.
     */
    ResponseEntity<String> addMember(MemberBO memberBO);

    /**
     * Updates the details of a member in the library.
     *
     * @param memberId The ID of the member to be updated.
     * @param memberBO The MemberBO object containing the updated member details.
     * @return ResponseEntity with a success message if the member is updated successfully, or a not found response if the member ID is invalid.
     */
    ResponseEntity<String> updateMember(int memberId, MemberBO memberBO);

    /**
     * Deletes a member from the library.
     *
     * @param memberId The ID of the member to be deleted.
     * @return ResponseEntity with a success message if the member is deleted successfully, or a not found response if the member ID is invalid.
     */
    ResponseEntity<String> deleteMember(int memberId);

    /**
     * Retrieves the books currently held by a member.
     *
     * @param memberId the ID of the member
     * @return ResponseEntity containing the list of books currently held by the member
     */
    ResponseEntity<List<Book>> getBooksForMember(int memberId);

}
