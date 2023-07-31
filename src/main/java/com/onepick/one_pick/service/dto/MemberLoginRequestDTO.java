package com.onepick.one_pick.service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MemberLoginRequestDTO {

    private String memberEmail;
    private String memberPassword;
}
