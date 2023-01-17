package com.example.actualjpa.api.controller;

import com.example.actualjpa.domain.Member;
import com.example.actualjpa.service.MemberService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class MemberApiController {

    private final MemberService memberService;

    @PostMapping("/api/v1/members")
    public CreateMemberResponse saveMemberV1(@RequestBody Member member) {

        Long id = memberService.join(member);
        return new CreateMemberResponse(id);
    }

    /*
     MEMO : ENTITY 통신 금지
     Controller - Presentation 계층
     >> Presentation 계층 에서 들어오는 것을 위해서 Entity 의 SPEC 이 변경될 수 있다.
     >> Entity 를 손대면 안됨. 외부에 노출해도 안됨. Entity 는 API 여러 곳에서 굉장히 많이 쓰이는데, 오직 Controller 단을 위해서??
     >> Presentation 계층을 위한 DTO 를 반드시 만들어줘야 한다 (API 요청 스펙에 맞춰서)
     >> Entity 노출 필요 없이 API SPEC 도 확실하게 알 수 있음
    */

    @PostMapping("/api/v2/members")
    public CreateMemberResponse saveMemberV2(@RequestBody CreateMemberRequestDto request) {

        // 서비스에서 할 일
        Member member = new Member();
        member.setName(request.getName());

        Long id = memberService.join(member);

        return new CreateMemberResponse(id);
    }

    @PutMapping("/api/v2/members/{memberId}")
    public UpdateMemberResponseDto updateMember(@PathVariable(value = "memberId") Long memberId
            , @RequestBody UpdateMemberRequestDto requestDto) {

//        Member findMember = memberService.findOne(memberId);
//        findMember.setName(requestDto.getName()); // 이렇게 하면 안바뀜 ㅋ // Transactional 범위 밖에서 변경이 되었기 때문.

        // MEMO :: UPDATE 문에서 객체를 반환하면 이미 영컨이 끊겼기 때문에, 걔를 가지고 뭐를 하면 안된다
        //         실수 방지를 위해서 아예 반환하지 않게 하거나, 확인용 memberId 만을 반환해주는게 좋다.

        Long savedMemberId = memberService.updateMemberName(memberId, requestDto.getName());

        // 새로 조회, update 함수의 규칙성을 지켜주기 위해 쿼리 한번 더 발생해도 이게 맞음. 문제 예방 차원.
        Member updatedMember = memberService.findOne(savedMemberId);

        return new UpdateMemberResponseDto(updatedMember.getId(), updatedMember.getName());
    }

    /*
     1. Entity 에 Presentation 계층을 위한 데이터 작업이 들어오면 안됨. 가령, Order 를 이 요청에서 보내기 싫어서 "@JsonIgnore" 를 추가한다.
     2. API 명세상 이름이 바뀐다? -> Entity 이름을 바꿔야 함. 이러면 문제가 됨. 반대여도 문제가 됨.
     3. 다른 API 들이 있을경우 다른 SPEC 을 요청할 수도 있음 (Address 만?) 그러면 모두 노출시키면 안됨 >> 그러면 또 Entity 를 건들여야 함
     >> 실제 Entity 는 간결하게 해야하는데, 실무는 엄청 복잡해서, 다양한 API 들이 생성될 수 있음.
     >> MEMO:  결국 같은 얘기, Entity 를 가지고 통신을 하면 안된다.
     */
    @GetMapping("/api/v1/members")
    public List<Member> memberV1() {
        return memberService.findMembers();
    }

    // 이 문제들을 해결할 v2 를 만들어보자
    @GetMapping("/api/v2/members")
    public Result<List<MemberDto>> memberV2() {

        List<Member> members = memberService.findMembers();
        List<MemberDto> memberDtos =
                members.stream().map((mem) -> new MemberDto(mem.getName())).collect(Collectors.toList());
        return new Result<>(memberDtos);
    }

    // 이런 응답서가 필요한 이유
    // 어떤 것이 필요하든 금방 넣을 수 있음
    @Data
    @AllArgsConstructor
    static class Result<T> {
        private T data;
    }

    @Data
    @AllArgsConstructor
    static class MemberDto {
        private String name;
    }


    @Data
    @AllArgsConstructor
    static class CreateMemberResponse {
        private Long id;
    }

    @Data
    static class CreateMemberRequestDto { // DTO 는 정석
        private String name;
    }

    @Data
    @AllArgsConstructor
    static class UpdateMemberResponseDto { // DTO 는 정석
        private Long id;
        private String name;
    }

    @Data
    static class UpdateMemberRequestDto { // DTO 는 정석
        private String name;
    }
}
