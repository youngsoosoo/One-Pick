package com.onepick.one_pick.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.onepick.one_pick.entity.Image;
import com.onepick.one_pick.entity.Member;
import com.onepick.one_pick.repository.ImageRepository;
import com.onepick.one_pick.service.dto.ImageRequestDTO;
import com.onepick.one_pick.service.dto.ImageSearchRequestDTO;
import com.onepick.one_pick.service.dto.ImageSearchResponseDTO;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Service
@RequiredArgsConstructor
public class ImageService {

    private final ImageRepository imageRepository;

    private final MemberService memberService;

    public void saveImage(ImageRequestDTO imageRequestDTO){

        Long memberId = imageRequestDTO.getMemberId();
        Member member = memberService.findMember(memberId);
        List<Image> images = new ArrayList<>();

        imageRequestDTO.getImagePaths().forEach(imagePath -> {

            images.add(Image.builder()
                .imagePath(imagePath)
                .member(member)
                .build());
        });

        imageRepository.saveAll(images);
    }

    public ImageSearchResponseDTO searchImage(ImageSearchRequestDTO imageSearchRequestDTO){

        Member member = memberService.findMember(imageSearchRequestDTO.getMemberId());
        List<Image> images = imageRepository.findByMember(member);
        log.info("키워드 출력: " + imageSearchRequestDTO.getKeyword());
        // AI 모델에 검색을 유도하는 메시지 전달
        return new ImageSearchResponseDTO(new ArrayList<>());
    }
}