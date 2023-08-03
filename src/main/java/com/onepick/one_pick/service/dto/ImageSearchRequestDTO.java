package com.onepick.one_pick.service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@RequiredArgsConstructor
public class ImageSearchRequestDTO {

    private Long memberId;
    private String keyword;
}
