package com.example.minecraftmodscatalog.controller;

import com.example.minecraftmodscatalog.dto.AsyncTaskStatusDto;
import com.example.minecraftmodscatalog.dto.AsyncTaskSubmissionDto;
import com.example.minecraftmodscatalog.dto.ConcurrentCounterDemoDto;
import com.example.minecraftmodscatalog.service.AsyncTaskService;
import com.example.minecraftmodscatalog.service.CounterDemoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/demo")
@RequiredArgsConstructor
@Tag(name = "Concurrency Demo", description = "Async, counter, and race condition examples")
public class ConcurrencyDemoController {
    private final AsyncTaskService asyncTaskService;
    private final CounterDemoService counterDemoService;

    @PostMapping("/async")
    @Operation(summary = "Submit an asynchronous business operation and receive a task ID")
    public ResponseEntity<AsyncTaskSubmissionDto> startAsyncTask(@RequestBody Map<String, Object> payload) {
        long delayMs = payload.get("delayMs") instanceof Number number ? number.longValue() : 0L;
        String label = payload.get("label") == null ? "demo-task" : payload.get("label").toString();
        return ResponseEntity.accepted().body(asyncTaskService.submitTask(delayMs, label));
    }

    @GetMapping("/async/{taskId}")
    @Operation(summary = "Check async task status by task ID")
    public ResponseEntity<AsyncTaskStatusDto> getAsyncTaskStatus(@PathVariable String taskId) {
        return ResponseEntity.ok(asyncTaskService.getTaskStatus(taskId));
    }

    @GetMapping("/counter")
    @Operation(summary = "Run a race condition demo with unsafe and Atomic counters")
    public ResponseEntity<ConcurrentCounterDemoDto> getCounterDemo(
            @RequestParam(defaultValue = "50") int threads,
            @RequestParam(defaultValue = "1000") int incrementsPerThread) {
        return ResponseEntity.ok(counterDemoService.runCounterDemo(threads, incrementsPerThread));
    }
}
