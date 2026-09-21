package com.example.minecraftmodscatalog.service.impl;

import com.example.minecraftmodscatalog.dto.AsyncTaskStatusDto;
import com.example.minecraftmodscatalog.dto.AsyncTaskSubmissionDto;
import com.example.minecraftmodscatalog.service.AsyncTaskService;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AsyncTaskServiceImpl implements AsyncTaskService {
    private final AsyncTaskWorker asyncTaskWorker;
    private final Map<String, CompletableFuture<AsyncTaskStatusDto>> taskStore = new ConcurrentHashMap<>();

    @Override
    public AsyncTaskSubmissionDto submitTask(long delayMs, String label) {
        String taskId = "task-" + System.nanoTime();
        CompletableFuture<AsyncTaskStatusDto> future = asyncTaskWorker.executeTask(taskId, delayMs, label);
        taskStore.put(taskId, future);
        return AsyncTaskSubmissionDto.builder()
                .taskId(taskId)
                .status("ACCEPTED")
                .message("Task accepted for asynchronous execution")
                .build();
    }

    @Override
    public AsyncTaskStatusDto getTaskStatus(String taskId) {
        CompletableFuture<AsyncTaskStatusDto> future = taskStore.get(taskId);
        if (future == null) {
            throw new IllegalArgumentException("Unknown task id: %s".formatted(taskId));
        }
        if (future.isDone()) {
            return future.join();
        }
        return AsyncTaskStatusDto.builder()
                .taskId(taskId)
                .status("RUNNING")
                .build();
    }

}
