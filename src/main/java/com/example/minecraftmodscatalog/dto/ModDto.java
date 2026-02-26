package com.example.minecraftmodscatalog.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ModDto {
    private Long id;
    private String name;
    private String description;
    private String author;
    private String version;
    private int downloadCount;
}