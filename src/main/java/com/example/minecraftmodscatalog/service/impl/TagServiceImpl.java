package com.example.minecraftmodscatalog.service.impl;

import com.example.minecraftmodscatalog.dto.TagCreateDto;
import com.example.minecraftmodscatalog.dto.TagDto;
import com.example.minecraftmodscatalog.entity.Tag;
import com.example.minecraftmodscatalog.mapper.TagMapper;
import com.example.minecraftmodscatalog.repository.ModRepository;
import com.example.minecraftmodscatalog.repository.TagRepository;
import com.example.minecraftmodscatalog.service.TagService;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TagServiceImpl implements TagService {
    private static final String TAG_NOT_FOUND_PREFIX = "Tag not found: ";
    private static final String TAG_NAME_ALREADY_EXISTS_PREFIX = "Tag name already exists: ";

    private final TagRepository tagRepository;
    private final ModRepository modRepository;

    @Override
    @Transactional(readOnly = true)
    public List<TagDto> getAllTags() {
        return tagRepository.findAll().stream()
                .map(TagMapper::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public TagDto getTagById(final Long id) {
        return tagRepository.findById(id)
                .map(TagMapper::toDto)
                .orElseThrow(() -> new EntityNotFoundException(TAG_NOT_FOUND_PREFIX + id));
    }

    @Override
    @Transactional
    public TagDto createTag(final TagCreateDto createDto) {
        validateName(createDto.getName());
        if (tagRepository.existsByNameIgnoreCase(createDto.getName())) {
            throw new IllegalArgumentException(TAG_NAME_ALREADY_EXISTS_PREFIX + createDto.getName());
        }

        Tag tag = new Tag();
        tag.setName(createDto.getName().trim());
        return TagMapper.toDto(tagRepository.save(tag));
    }

    @Override
    @Transactional
    public TagDto updateTag(final Long id, final TagCreateDto updateDto) {
        Tag tag = tagRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(TAG_NOT_FOUND_PREFIX + id));
        validateName(updateDto.getName());
        if (tagRepository.existsByNameIgnoreCaseAndIdNot(updateDto.getName(), id)) {
            throw new IllegalArgumentException(TAG_NAME_ALREADY_EXISTS_PREFIX + updateDto.getName());
        }
        tag.setName(updateDto.getName().trim());
        return TagMapper.toDto(tagRepository.save(tag));
    }

    @Override
    @Transactional
    public void deleteTag(final Long id) {
        Tag tag = tagRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(TAG_NOT_FOUND_PREFIX + id));

        // Remove this tag from all mods that use it
        modRepository.findAll().forEach(mod -> mod.getTags().remove(tag));
        modRepository.flush();

        tagRepository.deleteById(id);
    }

    private void validateName(final String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Tag name is required");
        }
    }
}
