package com.example.minecraftmodscatalog.service.impl;

import com.example.minecraftmodscatalog.dto.*;
import com.example.minecraftmodscatalog.entity.Author;
import com.example.minecraftmodscatalog.entity.Category;
import com.example.minecraftmodscatalog.entity.Mod;
import com.example.minecraftmodscatalog.entity.ModVersion;
import com.example.minecraftmodscatalog.entity.Tag;
import com.example.minecraftmodscatalog.repository.AuthorRepository;
import com.example.minecraftmodscatalog.repository.CategoryRepository;
import com.example.minecraftmodscatalog.repository.ModRepository;
import com.example.minecraftmodscatalog.repository.ModVersionRepository;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.Mockito.*;

public class ServiceImplMockitoTest {

    @ExtendWith(MockitoExtension.class)
    static class AuthorServiceImplTest {
        @Mock
        private AuthorRepository authorRepository;

        @Mock
        private ModRepository modRepository;

        @InjectMocks
        private AuthorServiceImpl service;

        @Test
        void createAuthor_shouldSaveTrimmedName() {
            AuthorCreateDto dto = new AuthorCreateDto();
            dto.setName("  Alice  ");
            Author saved = new Author();
            saved.setId(1L);
            saved.setName("Alice");

            when(authorRepository.existsByNameIgnoreCase("  Alice  ")).thenReturn(false);
            when(authorRepository.save(any(Author.class))).thenReturn(saved);

            var result = service.createAuthor(dto);

            assertThat(result.getName()).isEqualTo("Alice");
            verify(authorRepository).save(any(Author.class));
        }

        @Test
        void deleteAuthor_shouldThrowIfUsedByMods() {
            when(authorRepository.existsById(10L)).thenReturn(true);
            when(modRepository.existsByAuthorId(10L)).thenReturn(true);

            assertThatThrownBy(() -> service.deleteAuthor(10L))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("used by mods");
        }
    }

    @ExtendWith(MockitoExtension.class)
    static class CategoryServiceImplTest {
        @Mock
        private CategoryRepository categoryRepository;

        @Mock
        private ModRepository modRepository;

        @InjectMocks
        private CategoryServiceImpl service;

        @Test
        void createCategory_shouldSaveTrimmedName() {
            CategoryCreateDto dto = new CategoryCreateDto();
            dto.setName("  Utility  ");
            Category saved = new Category();
            saved.setId(2L);
            saved.setName("Utility");

            when(categoryRepository.existsByNameIgnoreCase("  Utility  ")).thenReturn(false);
            when(categoryRepository.save(any(Category.class))).thenReturn(saved);

            var result = service.createCategory(dto);

            assertThat(result.getName()).isEqualTo("Utility");
            verify(categoryRepository).save(any(Category.class));
        }

        @Test
        void deleteCategory_shouldThrowIfUsedByMods() {
            when(categoryRepository.existsById(5L)).thenReturn(true);
            when(modRepository.existsByCategoryId(5L)).thenReturn(true);

            assertThatThrownBy(() -> service.deleteCategory(5L))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("used by mods");
        }
    }

    @ExtendWith(MockitoExtension.class)
    static class TagServiceImplTest {
        @Mock
        private TagRepository tagRepository;

        @Mock
        private ModRepository modRepository;

        @InjectMocks
        private TagServiceImpl service;

        @Test
        void createTag_shouldSaveTrimmedName() {
            TagCreateDto dto = new TagCreateDto();
            dto.setName("  tech  ");
            Tag saved = new Tag();
            saved.setId(3L);
            saved.setName("tech");

            when(tagRepository.existsByNameIgnoreCase("  tech  ")).thenReturn(false);
            when(tagRepository.save(any(Tag.class))).thenReturn(saved);

            var result = service.createTag(dto);

            assertThat(result.getName()).isEqualTo("tech");
            verify(tagRepository).save(any(Tag.class));
        }

        @Test
        void deleteTag_shouldDetachFromModsAndDelete() {
            Tag tag = new Tag();
            tag.setId(9L);
            tag.setName("tech");
            Mod mod = new Mod();
            mod.setTags(Set.of(tag));

            when(tagRepository.findById(9L)).thenReturn(Optional.of(tag));
            when(modRepository.findAll()).thenReturn(List.of(mod));

            service.deleteTag(9L);

            assertThat(mod.getTags()).doesNotContain(tag);
            verify(modRepository).flush();
            verify(tagRepository).deleteById(9L);
        }
    }

    @ExtendWith(MockitoExtension.class)
    static class ModVersionServiceImplTest {
        @Mock
        private ModVersionRepository modVersionRepository;

        @Mock
        private ModRepository modRepository;

        @InjectMocks
        private ModVersionServiceImpl service;

        @Test
        void createVersion_shouldSaveWhenModExists() {
            ModVersionUpsertDto dto = new ModVersionUpsertDto();
            dto.setModId(7L);
            dto.setVersionName("  1.2.3  ");
            dto.setDownloadCount(150);

            Mod mod = new Mod();
            mod.setId(7L);
            mod.setName("Alpha");

            ModVersion saved = new ModVersion();
            saved.setId(11L);
            saved.setVersionName("1.2.3");
            saved.setDownloadCount(150);
            saved.setMod(mod);

            when(modRepository.findById(7L)).thenReturn(Optional.of(mod));
            when(modVersionRepository.save(any(ModVersion.class))).thenReturn(saved);

            var result = service.createVersion(dto);

            assertThat(result.getVersionName()).isEqualTo("1.2.3");
            assertThat(result.getDownloadCount()).isEqualTo(150);
            verify(modVersionRepository).save(any(ModVersion.class));
        }

        @Test
        void deleteVersion_shouldThrowWhenOnlyOneVersionExists() {
            ModVersion version = new ModVersion();
            Mod mod = new Mod();
            mod.setId(12L);
            version.setMod(mod);

            when(modVersionRepository.findById(2L)).thenReturn(Optional.of(version));
            when(modVersionRepository.countByModId(12L)).thenReturn(1L);

            assertThatThrownBy(() -> service.deleteVersion(2L))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("last version");
        }
    }

    @ExtendWith(MockitoExtension.class)
    static class ModServiceImplTest {
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
}
