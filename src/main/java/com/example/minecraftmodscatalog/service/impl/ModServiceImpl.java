package com.example.minecraftmodscatalog.service.impl;

import com.example.minecraftmodscatalog.dto.ModCreateDto;
import com.example.minecraftmodscatalog.dto.ModDto;
import com.example.minecraftmodscatalog.entity.Mod;
import com.example.minecraftmodscatalog.entity.ModVersion;
import com.example.minecraftmodscatalog.entity.Author;
import com.example.minecraftmodscatalog.entity.Category;
import com.example.minecraftmodscatalog.entity.Tag;
import com.example.minecraftmodscatalog.mapper.ModMapper;
import com.example.minecraftmodscatalog.repository.AuthorRepository;
import com.example.minecraftmodscatalog.repository.CategoryRepository;
import com.example.minecraftmodscatalog.repository.ModRepository;
import com.example.minecraftmodscatalog.repository.TagRepository;
import com.example.minecraftmodscatalog.service.ModService;
import jakarta.persistence.EntityNotFoundException;
import java.util.HashSet;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ModServiceImpl implements ModService {
    private static final String MOD_NOT_FOUND_PREFIX = "Mod not found: ";

    private final ModRepository modRepository;
    private final AuthorRepository authorRepository;
    private final CategoryRepository categoryRepository;
    private final TagRepository tagRepository;

    @Override
    @Transactional(readOnly = true)
    public List<ModDto> getAllMods() {
        return modRepository.findAllWithGraph().stream()
                .map(ModMapper::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ModDto> getModsByAuthor(final String authorName) {
        return modRepository.findByAuthorName(authorName).stream()
                .map(ModMapper::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ModDto getModById(final Long id) {
        return modRepository.findByIdWithGraph(id)
                .map(ModMapper::toDto)
                .orElseThrow(() -> new EntityNotFoundException(MOD_NOT_FOUND_PREFIX + id));
    }

    @Override
    @Transactional
    public ModDto createMod(final ModCreateDto createDto) {
        Mod saved = modRepository.save(buildModGraph(createDto));
        return ModMapper.toDto(saved);
    }

    @Override
    @Transactional
    public ModDto updateMod(final Long id, final ModCreateDto updateDto) {
        Mod existing = modRepository.findByIdWithGraph(id)
                .orElseThrow(() -> new EntityNotFoundException(MOD_NOT_FOUND_PREFIX + id));
        existing.setName(updateDto.getName());
        existing.setDescription(updateDto.getDescription());
        existing.setAuthor(resolveAuthor(updateDto.getAuthorName()));
        existing.setCategories(resolveCategories(updateDto.getCategoryNames()));
        existing.setTags(resolveTags(updateDto.getTagNames()));

        existing.getVersions().clear();
        for (var versionDto : updateDto.getVersions()) {
            ModVersion version = new ModVersion();
            version.setVersionName(versionDto.getVersionName());
            version.setDownloadCount(versionDto.getDownloadCount());
            version.setMod(existing);
            existing.getVersions().add(version);
        }
        return ModMapper.toDto(modRepository.save(existing));
    }

    @Override
    @Transactional
    public void deleteMod(final Long id) {
        Mod mod = modRepository.findByIdWithGraph(id)
                .orElseThrow(() -> new EntityNotFoundException(MOD_NOT_FOUND_PREFIX + id));

        Long authorId = mod.getAuthor().getId();
        modRepository.delete(mod);
        modRepository.flush();

        if (!modRepository.existsByAuthorId(authorId)) {
            authorRepository.deleteById(authorId);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<ModDto> getModsNaiveNPlusOne() {
        return modRepository.findAllNaiveForNPlusOne().stream()
                .map(ModMapper::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ModDto> getModsWithEntityGraph() {
        return modRepository.findAllWithGraph().stream()
                .map(ModMapper::toDto)
                .toList();
    }

    @Override
    public ModDto saveGraphWithoutTransactionAndFail(final ModCreateDto createDto) {
        Mod saved = modRepository.save(buildModGraph(createDto));
        throw new IllegalStateException("Failure without @Transactional. Mod already persisted with id="
                + saved.getId());
    }

    @Override
    @Transactional
    public ModDto saveGraphWithTransactionAndFail(final ModCreateDto createDto) {
        Mod saved = modRepository.save(buildModGraph(createDto));
        if (!saved.getVersions().isEmpty()) {
            throw new IllegalStateException("Failure inside @Transactional. Full rollback expected.");
        }
        return ModMapper.toDto(saved);
    }

    private Mod buildModGraph(final ModCreateDto createDto) {
        Mod mod = new Mod();
        mod.setName(createDto.getName());
        mod.setDescription(createDto.getDescription());
        mod.setAuthor(resolveAuthor(createDto.getAuthorName()));
        mod.setCategories(resolveCategories(createDto.getCategoryNames()));
        mod.setTags(resolveTags(createDto.getTagNames()));

        for (var versionDto : createDto.getVersions()) {
            ModVersion version = new ModVersion();
            version.setVersionName(versionDto.getVersionName());
            version.setDownloadCount(versionDto.getDownloadCount());
            version.setMod(mod);
            mod.getVersions().add(version);
        }
        return mod;
    }

    private Author resolveAuthor(final String authorName) {
        return authorRepository.findByNameIgnoreCase(authorName)
                .orElseGet(() -> {
                    Author author = new Author();
                    author.setName(authorName);
                    return authorRepository.save(author);
                });
    }

    private Set<Category> resolveCategories(final List<String> categoryNames) {
        Set<Category> categories = new HashSet<>();
        if (categoryNames == null) {
            return categories;
        }
        for (String categoryName : categoryNames) {
            Category category = categoryRepository.findByNameIgnoreCase(categoryName)
                    .orElseGet(() -> {
                        Category newCategory = new Category();
                        newCategory.setName(categoryName);
                        return categoryRepository.save(newCategory);
                    });
            categories.add(category);
        }
        return categories;
    }

    private Set<Tag> resolveTags(final List<String> tagNames) {
        Set<Tag> tags = new HashSet<>();
        if (tagNames == null) {
            return tags;
        }
        for (String tagName : tagNames) {
            Tag tag = tagRepository.findByNameIgnoreCase(tagName)
                    .orElseGet(() -> {
                        Tag newTag = new Tag();
                        newTag.setName(tagName);
                        return tagRepository.save(newTag);
                    });
            tags.add(tag);
        }
        return tags;
    }
}
