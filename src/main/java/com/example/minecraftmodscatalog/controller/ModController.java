package com.example.minecraftmodscatalog.controller;

import com.example.minecraftmodscatalog.dto.ModCreateDto;
import com.example.minecraftmodscatalog.dto.ModDto;
import com.example.minecraftmodscatalog.dto.TransactionDemoResultDto;
import com.example.minecraftmodscatalog.service.ModService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/mods")
@RequiredArgsConstructor
public class ModController {
    private final ModService modService;

    @GetMapping
    public ResponseEntity<List<ModDto>> getMods(@RequestParam(required = false) final String author) {
        if (author != null && !author.isBlank()) {
            return ResponseEntity.ok(modService.getModsByAuthor(author));
        }
        return ResponseEntity.ok(modService.getAllMods());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ModDto> getModById(@PathVariable final Long id) {
        return ResponseEntity.ok(modService.getModById(id));
    }

    @PostMapping
    public ResponseEntity<ModDto> createMod(@RequestBody final ModCreateDto createDto) {
        return ResponseEntity.status(201).body(modService.createMod(createDto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ModDto> updateMod(
            @PathVariable final Long id,
            @RequestBody final ModCreateDto updateDto
    ) {
        return ResponseEntity.ok(modService.updateMod(id, updateDto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMod(@PathVariable final Long id) {
        modService.deleteMod(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/nplus1/naive")
    public ResponseEntity<List<ModDto>> naiveNPlusOne() {
        return ResponseEntity.ok(modService.getModsNaiveNPlusOne());
    }

    @GetMapping("/nplus1/entity-graph")
    public ResponseEntity<List<ModDto>> solvedNPlusOne() {
        return ResponseEntity.ok(modService.getModsWithEntityGraph());
    }

    @PostMapping("/demo/without-transaction")
    public ResponseEntity<TransactionDemoResultDto> withoutTransaction(
            @RequestBody final ModCreateDto createDto
    ) {
        try {
            modService.saveGraphWithoutTransactionAndFail(createDto);
            return ResponseEntity.ok(new TransactionDemoResultDto(true, "without-transaction", "No error happened"));
        } catch (RuntimeException ex) {
            return ResponseEntity.internalServerError()
                    .body(new TransactionDemoResultDto(false, "without-transaction", ex.getMessage()));
        }
    }

    @PostMapping("/demo/with-transaction")
    public ResponseEntity<TransactionDemoResultDto> withTransaction(
            @RequestBody final ModCreateDto createDto
    ) {
        try {
            modService.saveGraphWithTransactionAndFail(createDto);
            return ResponseEntity.ok(new TransactionDemoResultDto(true, "with-transaction", "No error happened"));
        } catch (RuntimeException ex) {
            return ResponseEntity.internalServerError()
                    .body(new TransactionDemoResultDto(false, "with-transaction", ex.getMessage()));
        }
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<String> handleNotFound(EntityNotFoundException ex) {
        return ResponseEntity.notFound().build();
    }
}
