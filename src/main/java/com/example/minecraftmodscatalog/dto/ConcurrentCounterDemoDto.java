package com.example.minecraftmodscatalog.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConcurrentCounterDemoDto {
    private long expectedTotal;
    private long unsafeCounter;
    private long atomicCounter;
    private boolean raceConditionDetected;
}
