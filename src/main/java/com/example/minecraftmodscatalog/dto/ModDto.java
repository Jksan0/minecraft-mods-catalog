package com.example.minecraftmodscatalog.dto;

import lombok.Data;
import java.util.List;

@Data
public class ModDto {
    private Long id;
    private String name;
    private String description;
    private String authorName;
    private List<String> categories;
    private List<String> tags;
    private List<ModVersionDto> versions;
}
