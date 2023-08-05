package com.onepick.one_pick.controller;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.onepick.one_pick.common.ApiResponse;
import com.onepick.one_pick.service.ImageService;
import com.onepick.one_pick.service.dto.ImageRequestDTO;
import com.onepick.one_pick.service.dto.ImageSearchRequestDTO;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@RestController
@RequiredArgsConstructor
@Log4j2
@RequestMapping("/images")
public class ImageController {

    private final ImageService imageService;

    @PostMapping("")
    public ApiResponse postImage(@RequestBody ImageRequestDTO imageRequestDTO){

        try {

            imageService.saveImage(imageRequestDTO);
            return ApiResponse.success("이미지 저장 성공");
        }catch (Exception e){

            log.error("이미지 저장 실패: " + e.getMessage(), e);
            return ApiResponse.fail(400, "이미지 저장 실패: " + e.getMessage());
        }
    }

    @PostMapping("/search")
    public ApiResponse<List<String>> postImageSearch(@RequestBody ImageSearchRequestDTO imageSearchRequestDTO) throws Exception {

        try {

            return ApiResponse.success("이미지 검색 성공", imageService.searchImage(imageSearchRequestDTO));
        }catch (Exception e){

            log.error("이미지 검색 실패: " + e.getMessage(), e);
            return ApiResponse.fail(400, "이미지 검색 실패: " + e.getMessage());
        }
    }
}
