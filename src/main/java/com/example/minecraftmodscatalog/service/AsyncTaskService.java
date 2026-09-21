package com.example.minecraftmodscatalog.service;

import com.example.minecraftmodscatalog.dto.AsyncTaskStatusDto;
import com.example.minecraftmodscatalog.dto.AsyncTaskSubmissionDto;

public interface AsyncTaskService {
    AsyncTaskSubmissionDto submitTask(long delayMs, String label);
    AsyncTaskStatusDto getTaskStatus(String taskId);
}
