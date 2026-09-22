package com.library.model;

import java.time.LocalDate;

/**
 * Concrete subclass of Person representing a library member.
 */
public class Member extends Person {
    private LocalDate joinDate;

    public Member() {
        super();
    }

    public Member(int id, String name, String email, String phone, LocalDate joinDate) {
        super(id, name, email, phone);
        this.joinDate = joinDate;
    }

    public LocalDate getJoinDate() {
        return joinDate;
    }

    public void setJoinDate(LocalDate joinDate) {
        this.joinDate = joinDate;
    }

    @Override
    public String getDisplayInfo() {
        return String.format("Member[ID=%d, Name=%s, Email=%s, Phone=%s, Joined=%s]",
                getId(), getName(), getEmail(), getPhone(), joinDate);
    }

    @Override
    public String toString() {
        return getDisplayInfo();
    }
}
