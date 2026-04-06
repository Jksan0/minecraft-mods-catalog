package com.example.minecraftmodscatalog.dto;

import java.util.List;
import lombok.Data;

@Data
public class ModCreateDto {
    private String name;
    private String description;
    private String authorName;
    private List<String> categoryNames;
    private List<String> tagNames;
    private List<ModVersionCreateDto> versions;
}
