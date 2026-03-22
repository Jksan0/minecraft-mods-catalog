package com.example.minecraftmodscatalog.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TransactionDemoResultDto {
    private boolean success;
    private String mode;
    private String message;
}
