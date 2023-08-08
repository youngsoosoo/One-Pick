package com.onepick.one_pick.config;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import net.coobird.thumbnailator.Thumbnails;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ListObjectsV2Request;
import com.amazonaws.services.s3.model.ListObjectsV2Result;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.amazonaws.util.IOUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Configuration
@EnableBatchProcessing
@RequiredArgsConstructor
@Log4j2
public class BatchConfig {

    private final JobBuilderFactory jobBuilderFactory;

    private final StepBuilderFactory stepBuilderFactory;

    private final AmazonS3 amazonS3;

    @Value("${aws.s3.bucket}")
    private String bucket;

    private Long memberId = null;

    //Rest API로 데이터를 가져온다.
    @Bean
    public ItemReader<Map<String, Object>> ItemReader() {

        // S3에서 데이터를 읽어오는 Reader
        return new ItemReader<Map<String, Object>>() {

            private Iterator<S3ObjectSummary> objectSummaryIterator;

            @Override
            public Map<String, Object> read() {

                if (objectSummaryIterator == null) {
                    objectSummaryIterator = method().iterator();
                }

                if (objectSummaryIterator.hasNext()) {
                    S3ObjectSummary objectSummary = objectSummaryIterator.next();
                    return processImage(objectSummary.getKey());
                }

                return null; // No more items to read
            }

            // S3 데이터 호출
            private List<S3ObjectSummary> method() {

                try {
                    log.info("memberId: " + memberId);
                    ListObjectsV2Request listObjectsRequest = new ListObjectsV2Request()
                        .withBucketName(bucket)
                        .withPrefix(memberId + "/");

                    ListObjectsV2Result listObjectsResult = amazonS3.listObjectsV2(listObjectsRequest);

                    return listObjectsResult.getObjectSummaries().stream()
                        .filter(objectSummary -> !objectSummary.getKey().startsWith(memberId + "/processed/"))
                        .collect(Collectors.toList());
                }catch (Exception e) {

                    log.error("S3 이미지 다운로드 실패: " + e.getMessage(), e);
                    throw new RuntimeException("S3 이미지 다운로드 실패: " + e.getMessage());
                }
            }
        };
    }


    private Map<String, Object> processImage(String key) {
        try (S3Object s3Object = amazonS3.getObject(bucket, key);
             S3ObjectInputStream objectInputStream = s3Object.getObjectContent()) {

            byte[] bytes = IOUtils.toByteArray(objectInputStream);

            Map<String, Object> imageInfo = new HashMap<>();
            imageInfo.put("key", key);
            imageInfo.put("bytes", bytes);

            return imageInfo;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Map<String, Object> imageResult = null;

    @Bean
    public ItemProcessor<Map<String, Object>, Map<String, Object>> itemProcessor() {

        // 이미지 리사이징을 수행하는 Processor
        return new ItemProcessor<Map<String, Object>, Map<String, Object>>() {

            @Override
            public Map<String, Object> process(Map<String, Object> image) throws Exception {

                byte[] bytes = null;
                String key = null;
                imageResult = new HashMap<>();

                if(image != null){
                     bytes = (byte[]) image.get("bytes");
                     key = (String) image.get("key");
                }
                try (ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
                     ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

                    int resizedWidth = 224;
                    int resizedHeight = 224;

                    Thumbnails.of(inputStream)
                        .size(resizedWidth, resizedHeight) // 224x224 크기로 리사이징
                        .outputFormat("jpeg")
                        .toOutputStream(outputStream);

                    imageResult.put("key", key);
                    imageResult.put("bytes", outputStream.toByteArray());

                    return imageResult;
                }
            }
        };
    }

    @Bean
    public ItemWriter<Map<String, Object>> ItemWriter(AmazonS3 amazonS3) {

        // 리사이즈 된 이미지를 S3에 저장하는 Writer
        return new ItemWriter<Map<String, Object>>() {

            @Override
            public void write(List<? extends Map<String, Object>> items) throws Exception {

                String resizedFolder = "resized"; // 리사이즈 된 이미지를 저장할 폴더 이름

                String key = null;
                byte[] bytes = null;

                for (Map<String, Object> imageBytes : items) {

                    key = (String) imageBytes.get("key");
                    bytes = (byte[]) imageBytes.get("bytes");
                    String[] parts = key.split("/");
                    String fileName = parts[parts.length - 1];

                    // 리사이즈 된 이미지를 저장할 폴더에 새로운 키를 생성합니다.
                    String resizedImageKey = memberId + "/" + resizedFolder + "/" + fileName;

                    try (ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes)) {
                        // 리사이즈 된 이미지를 S3에 저장합니다.
                        ObjectMetadata metadata = new ObjectMetadata();
                        metadata.setContentLength(bytes.length);
                        amazonS3.putObject(new PutObjectRequest("one-pick", resizedImageKey, inputStream, metadata));
                    }
                }
            }
        };
    }

    @Bean
    public Step myStep(ItemReader<Map<String, Object>> itemReader, ItemProcessor<Map<String, Object>, Map<String, Object>> itemProcessor, ItemWriter<Map<String, Object>> itemWriter) {

        return stepBuilderFactory.get("myStep")
            .<Map<String, Object>, Map<String, Object>>chunk(10)
            .reader(itemReader)
            .processor(itemProcessor)
            .writer(itemWriter)
            .build();
    }

    Long startTime = null;
    Long endTime = null;

    @Bean
    public Job myJob(Step mystep){

        return this.jobBuilderFactory.get("myJob")
            .incrementer(new RunIdIncrementer())
            .start(mystep)
            .listener(new JobExecutionListener() {
                @Override
                public void beforeJob(JobExecution jobExecution) {
                    memberId = jobExecution.getJobParameters().getLong("memberId");
                    startTime = System.currentTimeMillis();
                }

                @Override
                public void afterJob(JobExecution jobExecution) {
                    if (jobExecution.getStatus() == BatchStatus.COMPLETED) {
                        endTime = System.currentTimeMillis();
                        log.info("Job 실행 종료, 걸리 시간: " + (endTime - startTime)/1000 + "s");
                    }
                }
            })
            .build();
    }
}
