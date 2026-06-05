package com.example.minecraftmodscatalog.controller;

import com.example.minecraftmodscatalog.config.ApiErrorResponses;
import com.example.minecraftmodscatalog.dto.TagCreateDto;
import com.example.minecraftmodscatalog.dto.TagDto;
import com.example.minecraftmodscatalog.service.TagService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tags")
@RequiredArgsConstructor
@Tag(name = "Tags", description = "Tag management API")
@ApiErrorResponses
public class TagController {
    private final TagService tagService;

    @GetMapping
    @Operation(summary = "Get all tags")
    public ResponseEntity<List<TagDto>> getAllTags() {
        return ResponseEntity.ok(tagService.getAllTags());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get tag by ID")
    public ResponseEntity<TagDto> getTagById(@PathVariable final Long id) {
        return ResponseEntity.ok(tagService.getTagById(id));
    }

    @PostMapping
    @Operation(summary = "Create a new tag")
    public ResponseEntity<TagDto> createTag(@Valid @RequestBody final TagCreateDto createDto) {
        return ResponseEntity.status(201).body(tagService.createTag(createDto));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update tag by ID")
    public ResponseEntity<TagDto> updateTag(
            @PathVariable final Long id,
            @Valid @RequestBody final TagCreateDto updateDto
    ) {
        return ResponseEntity.ok(tagService.updateTag(id, updateDto));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete tag by ID")
    public ResponseEntity<Void> deleteTag(@PathVariable final Long id) {
        tagService.deleteTag(id);
        return ResponseEntity.noContent().build();
    }
}
