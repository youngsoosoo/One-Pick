package com.onepick.one_pick.service;

import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.onepick.one_pick.entity.Member;
import com.onepick.one_pick.entity.MemberRole;
import com.onepick.one_pick.exception.MemberEmailAlreadyExistsException;
import com.onepick.one_pick.repository.MemberRepository;
import com.onepick.one_pick.service.dto.MemberJoinDTO;
import com.onepick.one_pick.service.dto.MemberLoginRequestDTO;
import com.onepick.one_pick.service.dto.MemberLoginResponseDTO;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;

    private final PasswordEncoder passwordEncoder;

    //회원가입
    public void join(MemberJoinDTO memberJoinDTO) {

        // 이메일 및 닉네임 중복확인
        validateSignUpInfo(memberJoinDTO);
        //회원 가입을 위해서 입력 받은 정보를 가지고 Member Entity를 생성
        Member member = Member.builder()
            .memberPassword(memberJoinDTO.getMemberPassword())
            .memberEmail(memberJoinDTO.getMemberEmail())
            .memberName(memberJoinDTO.getMemberName())
            .del(false)
            .social("LOCAL")
            .build();

        //비밀번호 암호화
        member.changePassword(passwordEncoder.encode(memberJoinDTO.getMemberPassword()));
        //권한 설정
        member.addRole(MemberRole.USER);
        memberRepository.save(member);
    }

    // 이메일 존재 확인
    private void validateSignUpInfo(MemberJoinDTO memberJoinDTO) {
        if(memberRepository.existsByMemberEmail(memberJoinDTO.getMemberEmail()))
            throw new MemberEmailAlreadyExistsException(memberJoinDTO.getMemberEmail());
    }

    // 로그인 메서드
    public MemberLoginResponseDTO login(MemberLoginRequestDTO memberLoginRequestDTO) {

        // 기존의 유저 정보 검색
        Optional<Member> member = memberRepository.findByEmail(memberLoginRequestDTO.getMemberEmail());

        if (member.isEmpty()) {

            // 사용자가 존재하지 않는 경우 예외 처리 또는 오류 메시지 반환
            throw new RuntimeException("User not found");
        }

        // 비밀번호 검증
        validatePassword(memberLoginRequestDTO, member.get());

        // 로컬 로그인
        if(member.get().getSocial().equals("LOCAL")){

            return new MemberLoginResponseDTO(member.get().getMemberId(), member.get().getMemberName());
        }else{
            throw new RuntimeException(member.get().getSocial() + "으로 로그인한 회원입니다.");
        }
    }

    // 비밀번호 검증
    private void validatePassword(MemberLoginRequestDTO memberLoginRequestDTO, Member member) {
        if(!passwordEncoder.matches(memberLoginRequestDTO.getMemberPassword(), member.getMemberPassword())) {
            throw new RuntimeException("비밀번호가 다릅니다.");
        }
    }

    // 회원 정보 찾기
    public Member findMember(Long memberId){

        return memberRepository.findById(memberId).orElseThrow(() -> new RuntimeException("회원 정보 찾기 실패"));
    }

    // 전처리 저장
    public void savePreprocess(Long memberId){

        Member member = findMember(memberId);
        member.setPreprocess(true);

        memberRepository.save(member);
    }
}