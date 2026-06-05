package com.example.minecraftmodscatalog.controller;

import com.example.minecraftmodscatalog.config.ApiErrorResponses;
import com.example.minecraftmodscatalog.dto.ModVersionDto;
import com.example.minecraftmodscatalog.dto.ModVersionUpsertDto;
import com.example.minecraftmodscatalog.service.ModVersionService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/mod-versions")
@RequiredArgsConstructor
@Tag(name = "Mod Versions", description = "Mod version management API")
@ApiErrorResponses
public class ModVersionController {
    private final ModVersionService modVersionService;

    @GetMapping
    @Operation(summary = "Get all versions or filter by mod ID")
    public ResponseEntity<List<ModVersionDto>> getAllVersions(
            @RequestParam(required = false) final Long modId
    ) {
        return ResponseEntity.ok(modVersionService.getAllVersions(modId));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get version by ID")
    public ResponseEntity<ModVersionDto> getVersionById(@PathVariable final Long id) {
        return ResponseEntity.ok(modVersionService.getVersionById(id));
    }

    @PostMapping
    @Operation(summary = "Create a new mod version")
    public ResponseEntity<ModVersionDto> createVersion(@Valid @RequestBody final ModVersionUpsertDto createDto) {
        return ResponseEntity.status(201).body(modVersionService.createVersion(createDto));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update mod version by ID")
    public ResponseEntity<ModVersionDto> updateVersion(
            @PathVariable final Long id,
            @Valid @RequestBody final ModVersionUpsertDto updateDto
    ) {
        return ResponseEntity.ok(modVersionService.updateVersion(id, updateDto));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete mod version by ID")
    public ResponseEntity<Void> deleteVersion(@PathVariable final Long id) {
        modVersionService.deleteVersion(id);
        return ResponseEntity.noContent().build();
    }
}
