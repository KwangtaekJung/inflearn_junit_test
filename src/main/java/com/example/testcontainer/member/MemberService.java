package com.example.testcontainer.member;


import com.example.testcontainer.domain.Member;
import com.example.testcontainer.domain.Study;

import java.util.Optional;

public interface MemberService {

    Optional<Member> findById(Long memberId);

    void validate(Long memberId);

    void notify(Study newstudy);

    void notify(Member member);
}