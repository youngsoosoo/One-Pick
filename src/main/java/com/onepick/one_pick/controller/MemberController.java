package com.onepick.one_pick.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.onepick.one_pick.common.ApiResponse;
import com.onepick.one_pick.service.MemberService;
import com.onepick.one_pick.service.dto.MemberJoinDTO;
import com.onepick.one_pick.service.dto.MemberLoginRequestDTO;
import com.onepick.one_pick.service.dto.MemberLoginResponseDTO;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@RestController
@Log4j2
@RequiredArgsConstructor
@RequestMapping("/member")
//member와 관련된 요청을 처리할 메서드
public class MemberController {

    private final MemberService memberService;

    //회원 가입 처리
    @PostMapping("/join")
    public ApiResponse join(@RequestBody MemberJoinDTO memberJoinDTO){

        try{

            // 성공
            memberService.join(memberJoinDTO);
            return ApiResponse.success("회원가입 성공");
        } catch (Exception e) {
            
            // 실패
            log.error("회원가입 실패: " + e.getMessage(), e);
            return ApiResponse.fail(400, "회원가입 실패");
        }
    }

    // 로그인
    @PostMapping("/login")
    public ApiResponse<MemberLoginResponseDTO> login(@RequestBody MemberLoginRequestDTO memberLoginRequestDTO) {

        try{
            MemberLoginResponseDTO memberLoginResponseDTO = memberService.login(memberLoginRequestDTO);
            return ApiResponse.success("로그인 성공", memberLoginResponseDTO);
        }catch (Exception e){
            log.error("로그인 실패: " + e.getMessage(), e);
            return ApiResponse.error(400, "로그인 실패: " + e.getMessage());
        }
    }
}