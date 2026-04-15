package com.example.minecraftmodscatalog.service;

import com.example.minecraftmodscatalog.dto.TagCreateDto;
import com.example.minecraftmodscatalog.dto.TagDto;
import java.util.List;

public interface TagService {
    List<TagDto> getAllTags();

    TagDto getTagById(Long id);

    TagDto createTag(TagCreateDto createDto);

    TagDto updateTag(Long id, TagCreateDto updateDto);

    void deleteTag(Long id);
}
