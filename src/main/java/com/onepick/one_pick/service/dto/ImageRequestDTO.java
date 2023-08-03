package com.onepick.one_pick.service.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@RequiredArgsConstructor
public class ImageRequestDTO {

    private Long memberId;
    private List<String> imagePaths;
}
