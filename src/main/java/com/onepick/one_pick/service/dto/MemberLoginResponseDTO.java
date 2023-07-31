package com.onepick.one_pick.service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class MemberLoginResponseDTO {

    private Long memberId;
    private String memberName;
}
