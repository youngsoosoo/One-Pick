package com.onepick.one_pick.config;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.annotation.BeforeStep;
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
import com.onepick.one_pick.config.listener.JobCompletionNotificationListener;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Configuration
@EnableBatchProcessing
@RequiredArgsConstructor
@Log4j2
public class BatchConfig {

    private final JobBuilderFactory jobBuilderFactory;

    private final StepBuilderFactory stepBuilderFactory;

    private List<Map<String, Object>> imageList = new ArrayList<>(); //Rest로 가져온 데이터를 리스트에 넣는다.
    private int nextIndex = 0;//리스트의 데이터를 하나씩 인덱스를 통해 가져온다.

    private final AmazonS3 amazonS3;

    @Value("${aws.s3.bucket}")
    private String bucket;

    private Long memberId;

    Map<String, Object> imageInfo = new HashMap<>();

    // S3 데이터 호출
    public void method(Long member) {

        try {
            memberId = member;
            nextIndex = 0;
            imageList = new ArrayList<>();
            ListObjectsV2Request listObjectsRequest = new ListObjectsV2Request()
                .withBucketName(bucket)
                .withPrefix(memberId + "/");

            ListObjectsV2Result listObjectsResult = amazonS3.listObjectsV2(listObjectsRequest);
            List<S3ObjectSummary> objectSummaries = listObjectsResult.getObjectSummaries();
            log.info(objectSummaries);

            objectSummaries.forEach(objectSummary -> {

                String key = objectSummary.getKey();

                if (!key.startsWith(memberId + "/processed/")){
                    S3Object s3Object = amazonS3.getObject(bucket, key);
                    try (S3ObjectInputStream objectInputStream = s3Object.getObjectContent()) {
                        byte[] bytes = IOUtils.toByteArray(objectInputStream);


                        imageInfo = new HashMap<>();
                        imageInfo.put("key", key);
                        imageInfo.put("bytes", bytes);

                        imageList.add(imageInfo);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
        } catch (Exception e) {

            log.error("S3 이미지 다운로드 실패: " + e.getMessage(), e);
        }
    }

    //Rest API로 데이터를 가져온다.
    @Bean
    public ItemReader<Map<String, Object>> ItemReader() {

        // S3에서 데이터를 읽어오는 Reader
        return new ItemReader<Map<String, Object>>() {

            @Override
            public Map<String, Object> read(){

                Map<String, Object> imageInfo = null;

                if (nextIndex < imageList.size()){
                    imageInfo = imageList.get(nextIndex);
                    nextIndex++;
                }

                return imageInfo;
            }
        };
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

                log.info("S3 데이터 삽입 시작");
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

    @Bean
    public Job myJob(Step mystep){

        return this.jobBuilderFactory.get("myJob")
            .incrementer(new RunIdIncrementer())
            .start(mystep)
            .listener(new JobCompletionNotificationListener())
            .build();
    }
}
