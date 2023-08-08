package com.onepick.one_pick.config.process;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectSummary;

import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.annotation.BeforeStep;
import org.springframework.batch.item.ItemReader;
import org.springframework.beans.factory.annotation.Value;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@RequiredArgsConstructor
@Log4j2
public class S3ItemReader implements ItemReader<byte[]> {

    private final AmazonS3 amazonS3;

    private final List<byte[]> imageList;

    @Value("${aws.s3.bucket}")
    private String bucket;

    private Long memberId;

    private Iterator<S3ObjectSummary> objectSummaryIterator;
    private List<S3Object> s3Objects;

    @BeforeStep
    public void beforeStep(StepExecution stepExecution) {
        this.memberId = stepExecution.getJobParameters().getLong("memberId"); // memberId 추출
    }

    @Override
    public byte[] read() throws Exception {

        imageList.forEach(image -> {
            log.info(memberId + " 이미지 출력: " + Arrays.toString(image));
        });


        // if (s3ObjectContents == null || s3ObjectContents.isEmpty()) {
        //     s3ObjectContents = new ArrayList<>();
        //     while (objectSummaryIterator.hasNext()) {
        //         S3ObjectSummary objectSummary = objectSummaryIterator.next();
        //         S3Object s3Object = amazonS3.getObject(bucket, objectSummary.getKey());
        //         try (S3ObjectInputStream objectInputStream = s3Object.getObjectContent()) {
        //             byte[] imageData = IOUtils.toByteArray(objectInputStream);
        //             s3ObjectContents.add(imageData);
        //         }
        //     }
        // }
        //
        // if (!s3ObjectContents.isEmpty()) {
        //     return s3ObjectContents.remove(0);
        // }

        return null;
    }

}