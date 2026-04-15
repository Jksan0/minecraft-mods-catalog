package com.example.minecraftmodscatalog.dto;

import lombok.Data;

@Data
public class ModVersionUpsertDto {
    private String versionName;
    private int downloadCount;
    private Long modId;
}
