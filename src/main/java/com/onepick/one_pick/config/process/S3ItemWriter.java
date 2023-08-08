package com.onepick.one_pick.config.process;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;

import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.annotation.BeforeStep;
import org.springframework.batch.item.ItemWriter;

import java.io.ByteArrayInputStream;
import java.util.List;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class S3ItemWriter implements ItemWriter<byte[]> {

    private final AmazonS3 amazonS3;

    private Long memberId;

    @BeforeStep
    public void beforeStep(StepExecution stepExecution) {
        this.memberId = stepExecution.getJobParameters().getLong("memberId"); // memberId 추출
    }

    @Override
    public void write(List<? extends byte[]> resizedImages) throws Exception {
        String resizedFolder = "resized"; // 리사이즈 된 이미지를 저장할 폴더 이름

        for (byte[] imageBytes : resizedImages) {
            // memberId 폴더에 저장된 원본 이미지의 키를 가져옵니다.
            String originalImageKey = memberId + "/" + System.currentTimeMillis() + ".jpg";

            // 리사이즈 된 이미지를 저장할 폴더에 새로운 키를 생성합니다.
            String resizedImageKey = memberId + "/" + resizedFolder + "/" + System.currentTimeMillis() + ".jpg";

            try (ByteArrayInputStream inputStream = new ByteArrayInputStream(imageBytes)) {
                // 리사이즈 된 이미지를 S3에 저장합니다.
                ObjectMetadata metadata = new ObjectMetadata();
                metadata.setContentLength(imageBytes.length);
                amazonS3.putObject(new PutObjectRequest("one-pick", resizedImageKey, inputStream, metadata));
            }
        }
    }
}

