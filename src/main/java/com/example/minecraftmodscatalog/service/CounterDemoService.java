package com.example.minecraftmodscatalog.service;

import com.example.minecraftmodscatalog.dto.ConcurrentCounterDemoDto;

public interface CounterDemoService {
    ConcurrentCounterDemoDto runCounterDemo(int threadCount, int incrementPerThread);
}
