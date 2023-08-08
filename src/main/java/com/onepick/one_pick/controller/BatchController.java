package com.onepick.one_pick.controller;

import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.onepick.one_pick.common.ApiResponse;
import com.onepick.one_pick.config.BatchConfig;
import com.onepick.one_pick.service.BatchService;
import com.onepick.one_pick.service.dto.BatchRequestDTO;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@RestController
@RequiredArgsConstructor
@Log4j2
@RequestMapping("/batch")
public class BatchController {


    private final BatchService batchService;

    @PostMapping("")
    public ApiResponse startBatch(@RequestBody BatchRequestDTO batchRequestDTO){

        try {

            batchService.startBatchAsync(batchRequestDTO);
            return ApiResponse.success("배치 작업 실행 완료");
        }catch (Exception e){
            log.error("배치 작업 실패" + e.getMessage(), e);
            return ApiResponse.fail(400, "배치 작업 실패" + e.getMessage());
        }
    }
}