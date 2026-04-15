package com.example.minecraftmodscatalog.controller;

import com.example.minecraftmodscatalog.dto.ModVersionDto;
import com.example.minecraftmodscatalog.dto.ModVersionUpsertDto;
import com.example.minecraftmodscatalog.service.ModVersionService;
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
public class ModVersionController {
    private final ModVersionService modVersionService;

    @GetMapping
    public ResponseEntity<List<ModVersionDto>> getAllVersions(
            @RequestParam(required = false) final Long modId
    ) {
        return ResponseEntity.ok(modVersionService.getAllVersions(modId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ModVersionDto> getVersionById(@PathVariable final Long id) {
        return ResponseEntity.ok(modVersionService.getVersionById(id));
    }

    @PostMapping
    public ResponseEntity<ModVersionDto> createVersion(@RequestBody final ModVersionUpsertDto createDto) {
        return ResponseEntity.status(201).body(modVersionService.createVersion(createDto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ModVersionDto> updateVersion(
            @PathVariable final Long id,
            @RequestBody final ModVersionUpsertDto updateDto
    ) {
        return ResponseEntity.ok(modVersionService.updateVersion(id, updateDto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteVersion(@PathVariable final Long id) {
        modVersionService.deleteVersion(id);
        return ResponseEntity.noContent().build();
    }
}
