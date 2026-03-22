package com.example.minecraftmodscatalog.dto;

import lombok.Data;

@Data
public class ModVersionDto {
    private Long id;
    private String versionName;
    private int downloadCount;
}
