package com.onepick.one_pick.service;

import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.onepick.one_pick.config.BatchConfig;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Service
@RequiredArgsConstructor
@Log4j2
public class BatchService {

    private final JobLauncher jobLauncher;

    private final BatchConfig batchConfig;

    private final Step myStep;

    @Async
    public void startBatchAsync(Long memberId) {

        try {
            JobParameters jobParameters = new JobParametersBuilder()
                .addLong("memberId", memberId)
                .toJobParameters();

            jobLauncher.run(batchConfig.myJob(myStep), jobParameters);
        } catch (Exception e) {
            log.error("배치 작업 실패: " + e.getMessage(), e);
        }
    }
}