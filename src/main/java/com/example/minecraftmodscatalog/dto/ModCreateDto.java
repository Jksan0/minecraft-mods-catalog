package com.example.minecraftmodscatalog.dto;

import lombok.Data;

@Data
public class ModCreateDto {
    private String name;
    private String description;
    private String author;
    private String version;
    private int downloadCount;
}