package com.onepick.one_pick.service.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class ImageSearchResponseDTO {

    List<String> imagePaths;
}
