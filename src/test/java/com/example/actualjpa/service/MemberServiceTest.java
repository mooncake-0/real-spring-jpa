package com.example.actualjpa.service;

import com.example.actualjpa.domain.Member;
import com.example.actualjpa.repository.MemberRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

/*
 단위 테스트가 아니라, JPA 가 직접 메모리 모드로 DB까지 돌고 하는 것을 보여줄 것임
 SpringBootTest 란 스프링 부트를 실제처럼 다 실행시켜서 테스트를 진행하는 것을 말함
 */

@SpringBootTest
@Transactional // 실제 DB에 입출력을 진행할 것임, 롤백을 위해서이기도 함
@Rollback(value = false) //
// Test에서의 Transactional 은 기본적으로 커밋을 안하고 Rollback 을 해버린다.
// 따라서 영컨이 커밋하는 것을 보고 싶으면, Rollback 을 해제시켜줘야함
// 당연함. Test 에서 한 DB 가 저장되면 안됨
// 그래서 그냥 Test 용 DB를 따로 SU 해야지.
public class MemberServiceTest {

    // Test Case 에서는 다른 클래스에서 참조할 일이 없으므로, 간단히 필드 주입해줘도 됨
    @Autowired MemberService memberService;
    @Autowired MemberRepository memberRepository;

    // 두 가지 검증
    // 1. 회원 가입에 성공해야 함
    // 2. 중복 검증을 통과해야 함

    @Test
    public void 회원가입() throws Exception {
        //given
        Member member = new Member();
        member.setName("KIM");

        //when
        Long memberId = memberService.join(member);

        // then
        // 저장한 녀석과, 저장하라고 한 녀석이 같아야 함
        Assertions.assertEquals(member, memberRepository.findOne(memberId));
    }


    @Test
    @DisplayName("MemberService Join 시 validate 함수의 동작성을 검증한다")
    public void 중복_회원_예외() throws Exception{
        //given
        Member memberA = new Member();
        memberA.setName("memberA");

        Member memberB = new Member();
        memberB.setName("memberA");

        //when
        memberService.join(memberA);

        // 1차로 가능한 방법,
//        try {
//            memberService.join(memberB);
//        } catch (IllegalStateException) {
//            return;
//        }
//
//        then (도달시 Fail)
//        Assertions.fail("예외가 발생해서 여기에 도달하면 안됩니다");

        // JUNIT5 에서 가능한 방법
        Assertions.assertThrows(IllegalStateException.class, () -> {
            memberService.join(memberB);
        });
    }
}
