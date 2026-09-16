package com.example.minecraftmodscatalog.service.impl;

import com.example.minecraftmodscatalog.dto.ModCreateDto;
import com.example.minecraftmodscatalog.dto.ModDto;
import com.example.minecraftmodscatalog.dto.ModVersionCreateDto;
import com.example.minecraftmodscatalog.dto.ModVersionDto;
import com.example.minecraftmodscatalog.entity.Author;
import com.example.minecraftmodscatalog.entity.Category;
import com.example.minecraftmodscatalog.entity.Mod;
import com.example.minecraftmodscatalog.entity.ModVersion;
import com.example.minecraftmodscatalog.repository.AuthorRepository;
import com.example.minecraftmodscatalog.repository.CategoryRepository;
import com.example.minecraftmodscatalog.repository.ModRepository;
import com.example.minecraftmodscatalog.repository.TagRepository;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ModServiceImplTest {
    @Mock
    private ModRepository modRepository;

    @Mock
    private AuthorRepository authorRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private TagRepository tagRepository;

    @InjectMocks
    private ModServiceImpl service;

    @Test
    void createMods_shouldPersistAndMapResult() {
        ModCreateDto dto = new ModCreateDto();
        dto.setName("Alpha");
        dto.setDescription("Desc");
        dto.setAuthorName("John");
        dto.setCategoryName("Utility");
        dto.setTagNames(List.of("tech", "magic"));

        ModVersionCreateDto versionDto = new ModVersionCreateDto();
        versionDto.setVersionName("1.0.0");
        versionDto.setDownloadCount(10);
        dto.setVersions(List.of(versionDto));

        Author author = new Author();
        author.setId(1L);
        author.setName("John");

        Category category = new Category();
        category.setId(2L);
        category.setName("Utility");

        when(authorRepository.findByNameIgnoreCase("John")).thenReturn(Optional.empty());
        when(authorRepository.save(any(Author.class))).thenAnswer(inv -> {
            Author a = inv.getArgument(0);
            a.setId(1L);
            return a;
        });
        when(categoryRepository.findByNameIgnoreCase("Utility")).thenReturn(Optional.empty());
        when(categoryRepository.save(any(Category.class))).thenAnswer(inv -> {
            Category c = inv.getArgument(0);
            c.setId(2L);
            return c;
        });
        when(tagRepository.findAllByLowerNameIn(anySet())).thenReturn(List.of());
        when(tagRepository.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));
        when(modRepository.existsByNameIgnoreCase("Alpha")).thenReturn(false);

        when(modRepository.saveAll(anyList())).thenAnswer(inv -> {
            List<Mod> sourceMods = inv.getArgument(0);
            List<Mod> mods = new java.util.ArrayList<>();
            for (Mod mod : sourceMods) {
                mod.setId(10L);
                mod.setAuthor(author);
                mod.setCategory(category);
                mod.setTags(Set.of());
                mods.add(mod);
            }
            return mods;
        });

        Mod saved = new Mod();
        saved.setId(10L);
        saved.setName("Alpha");
        saved.setDescription("Desc");
        saved.setAuthor(author);
        saved.setCategory(category);
        saved.setTags(Set.of());
        ModVersion version = new ModVersion();
        version.setId(99L);
        version.setVersionName("1.0.0");
        version.setDownloadCount(10);
        version.setMod(saved);
        saved.setVersions(List.of(version));

        when(modRepository.findAllWithGraphByIdIn(anyList())).thenReturn(List.of(saved));

        List<ModDto> result = service.createMods(List.of(dto));

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getName()).isEqualTo("Alpha");
        assertThat(result.getFirst().getVersions()).extracting(ModVersionDto::getVersionName).contains("1.0.0");
    }
}
