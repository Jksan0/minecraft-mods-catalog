package com.example.minecraftmodscatalog.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AsyncTaskStatusDto {
    private String taskId;
    private String status;
    private String result;
}
