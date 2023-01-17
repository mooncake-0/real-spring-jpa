package com.example.actualjpa.service;

import com.example.actualjpa.domain.Member;
import com.example.actualjpa.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;

    // 필요한 함수들 먼저 명세
    // 회원 가입 필요
    @Transactional // 얘만 조회용이 아니므로
    public Long join(Member member) {

        // 중복을 검증해주자
        validateDuplicateMember(member);

        memberRepository.save(member);

        return member.getId();
    }

    // 회원 전체 조회 필요
    public List<Member> findMembers() {
        return memberRepository.findAll();
    }

    // 회원 하나 조회
    public Member findOne(Long memberId) {
        return memberRepository.findOne(memberId);
    }


    // 기능 로직 함수
    // 문제 : WAS 가 동시에 여러개가 뜸.
    //      이름이  memberA 인 애가 동시에 db insert를 하게 되면, 이 로직을 동시에 호출함
    //      그러면 둘다 가입하게 됨.
    //      따라서 실무에서는 최후의 방어를 꼭 해야함. (유니크 제약조건이 꼭 필요)
    private void validateDuplicateMember(Member member) {
        List<Member> findMembers = memberRepository.findByName(member.getName());

        if (!findMembers.isEmpty()) {
            throw new IllegalStateException("이미 존재하는 회원입니다");
        }
    }

    @Transactional
    public Long updateMemberName(Long memberId, String memberName) {
        Member findMember = memberRepository.findOne(memberId);
        findMember.setName(memberName);
        return findMember.getId();
    }

}

