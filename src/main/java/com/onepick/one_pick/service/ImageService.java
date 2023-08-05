package com.onepick.one_pick.service;

import java.io.IOException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.util.IOUtils;
import com.onepick.one_pick.entity.Image;
import com.onepick.one_pick.entity.Member;
import com.onepick.one_pick.repository.ImageRepository;
import com.onepick.one_pick.service.dto.ImageRequestDTO;
import com.onepick.one_pick.service.dto.ImageSearchRequestDTO;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Service
@RequiredArgsConstructor
public class ImageService {

    private final ImageRepository imageRepository;

    private final MemberService memberService;

    @Value("${one.pick}")
    private String onePickUrl;

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
    @Value("${festApi.url}")
    private String apiUrl;

    // 이미지 검색 메서드
    public List<String> searchImage(ImageSearchRequestDTO imageSearchRequestDTO) throws Exception {

        String keyword = imageSearchRequestDTO.getKeyword();
        Long memberId = imageSearchRequestDTO.getMemberId();

        Member member = memberService.findMember(memberId);

        // AI 모델에 검색을 요청하는 메시지
        RestTemplate restTemplate = new RestTemplate();
        String encodedKeyword = URLEncoder.encode(keyword, StandardCharsets.UTF_8);
        String[] filePathList = null;

        if (member.isPreprocess()){
            filePathList = restTemplate.getForObject(apiUrl + encodedKeyword + "/" + memberId, String[].class);
        }else{
            throw new RuntimeException("이미지 전처리 작업 필요!");
        }

        List<String> fileNameList = new ArrayList<>();

        // filePathList를 사용하여 원하는 작업 수행
        for (String filePath : filePathList) {

            // filePath에 대한 작업 수행
            String[] urlParts = filePath.split("/");

            // 배열의 마지막 요소를 파일명으로 추출
            fileNameList.add(urlParts[urlParts.length - 1]);
        }

        return getObject(fileNameList, memberId);
    }

    private final AmazonS3 amazonS3;

    @Value("${aws.s3.bucket}")
    private String bucket;

    /**
     * S3 bucket 파일 다운로드
     */
    public List<String> getObject(List<String> fileNameList, Long memberId) throws IOException {

        List<String> imageList = new ArrayList<>();
        fileNameList.forEach(fileName -> {

            try {
                S3Object o = amazonS3.getObject(new GetObjectRequest(bucket + "/" + memberId, fileName));
                S3ObjectInputStream objectInputStream = o.getObjectContent();

                byte[] bytes = IOUtils.toByteArray(objectInputStream);
                imageList.add(Arrays.toString(bytes));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        
        return imageList;
    }

    @Async
    public void imagePreprocess(Long memberId){

        // AI 모델에 검색을 요청하는 메시지
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> responseEntity = restTemplate.postForEntity(apiUrl + memberId, null, String.class);
        String result = responseEntity.getBody();

        if (result.equals("done")){
            memberService.savePreprocess(memberId);
        }
    }
}