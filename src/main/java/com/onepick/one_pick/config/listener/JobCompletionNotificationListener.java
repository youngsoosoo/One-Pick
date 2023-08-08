package com.onepick.one_pick.config.listener;

import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.stereotype.Component;

import lombok.extern.log4j.Log4j2;

@Component
@Log4j2
public class JobCompletionNotificationListener implements JobExecutionListener {

    private Long startTime;
    private Long endTime;

    @Override
    public void beforeJob(JobExecution jobExecution) {
        startTime = System.currentTimeMillis();
    }

    @Override
    public void afterJob(JobExecution jobExecution) {

        if (jobExecution.getStatus() == BatchStatus.COMPLETED) {
            endTime = System.currentTimeMillis();
            log.info("Job 실행 종료, 걸리 시간: " + (endTime - startTime)/1000 + "s");
        }
    }
}