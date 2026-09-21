package com.example.minecraftmodscatalog.service.impl;

import com.example.minecraftmodscatalog.dto.ConcurrentCounterDemoDto;
import com.example.minecraftmodscatalog.service.CounterDemoService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.stereotype.Service;

@Service
public class CounterDemoServiceImpl implements CounterDemoService {

    @Override
    public ConcurrentCounterDemoDto runCounterDemo(int threadCount, int incrementPerThread) {
        if (threadCount <= 0 || incrementPerThread <= 0) {
            throw new IllegalArgumentException("threadCount and incrementPerThread must be positive");
        }

        int expected = threadCount * incrementPerThread;
        int[] unsafeCounter = {0};
        AtomicInteger atomicCounter = new AtomicInteger(0);
        ExecutorService executor = Executors.newFixedThreadPool(Math.max(threadCount, 8));

        try {
            for (int threadIndex = 0; threadIndex < threadCount; threadIndex++) {
                executor.submit(() -> {
                    for (int index = 0; index < incrementPerThread; index++) {
                        unsafeCounter[0]++;
                    }
                });
            }
            executor.shutdown();
            executor.awaitTermination(30, TimeUnit.SECONDS);

            ExecutorService atomicExecutor = Executors.newFixedThreadPool(Math.max(threadCount, 8));
            try {
                for (int threadIndex = 0; threadIndex < threadCount; threadIndex++) {
                    atomicExecutor.submit(() -> {
                        for (int index = 0; index < incrementPerThread; index++) {
                            atomicCounter.incrementAndGet();
                        }
                    });
                }
                atomicExecutor.shutdown();
                atomicExecutor.awaitTermination(30, TimeUnit.SECONDS);
            } finally {
                if (!atomicExecutor.isTerminated()) {
                    atomicExecutor.shutdownNow();
                }
            }

            return ConcurrentCounterDemoDto.builder()
                    .expectedTotal(expected)
                    .unsafeCounter(unsafeCounter[0])
                    .atomicCounter(atomicCounter.get())
                    .raceConditionDetected(unsafeCounter[0] != expected)
                    .build();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Counter demo interrupted", e);
        } finally {
            if (!executor.isTerminated()) {
                executor.shutdownNow();
            }
        }
    }
}
