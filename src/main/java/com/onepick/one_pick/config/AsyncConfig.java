package com.onepick.one_pick.config;

import java.util.concurrent.Executor;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@EnableAsync
public class AsyncConfig implements AsyncConfigurer {

    @Override
    public Executor getAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        // 프리티어 인스턴스의 CPU 코어 개수를 확인하여 설정
        int cpuCores = Runtime.getRuntime().availableProcessors();
        executor.setCorePoolSize(cpuCores); // 동시에 실행될 스레드 개수를 CPU 코어 개수로 설정

        // 최대 스레드 개수는 CPU 코어 개수의 몇 배 정도로 설정하거나 적절히 조정
        int maxThreads = cpuCores * 2;
        executor.setMaxPoolSize(maxThreads);

        // 큐의 최대 용량을 적절히 조정
        int queueCapacity = maxThreads * 2;
        executor.setQueueCapacity(queueCapacity);

        executor.setThreadNamePrefix("AsyncExecutor-");
        executor.initialize();
        return executor;
    }

    // 다른 비동기 설정 관련 메서드들 ...
}
