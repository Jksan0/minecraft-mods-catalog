package com.example.minecraftmodscatalog.service.impl;

import com.example.minecraftmodscatalog.dto.AsyncTaskStatusDto;
import java.util.concurrent.CompletableFuture;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class AsyncTaskWorker {

    @Async("asyncTaskExecutor")
    public CompletableFuture<AsyncTaskStatusDto> executeTask(String taskId, long delayMs, String label) {
        try {
            if (delayMs > 0) {
                Thread.sleep(delayMs);
            }
            String result = "Task '%s' processed successfully".formatted(label);
            return CompletableFuture.completedFuture(AsyncTaskStatusDto.builder()
                    .taskId(taskId)
                    .status("COMPLETED")
                    .result(result)
                    .build());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return CompletableFuture.completedFuture(AsyncTaskStatusDto.builder()
                    .taskId(taskId)
                    .status("FAILED")
                    .result(e.getMessage())
                    .build());
        }
    }
}
