package com.onepick.one_pick.config.process;

import org.springframework.batch.item.ItemProcessor;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import net.coobird.thumbnailator.Thumbnails;

public class S3ItemProcessor implements ItemProcessor<byte[], byte[]> {

    @Override
    public byte[] process(byte[] image) throws Exception {
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(image);
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            int resizedWidth = 224;
            int resizedHeight = 224;

            Thumbnails.of(inputStream)
                .size(resizedWidth, resizedHeight) // 224x224 크기로 리사이징
                .outputFormat("jpeg")
                .toOutputStream(outputStream);

            return outputStream.toByteArray();
        }
    }
}
