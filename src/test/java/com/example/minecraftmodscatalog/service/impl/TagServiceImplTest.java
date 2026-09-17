package com.example.minecraftmodscatalog.service.impl;

import com.example.minecraftmodscatalog.dto.TagCreateDto;
import com.example.minecraftmodscatalog.dto.TagDto;
import com.example.minecraftmodscatalog.entity.Mod;
import com.example.minecraftmodscatalog.entity.Tag;
import com.example.minecraftmodscatalog.repository.ModRepository;
import com.example.minecraftmodscatalog.repository.TagRepository;
import jakarta.persistence.EntityNotFoundException;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TagServiceImplTest {
    @Mock
    private TagRepository tagRepository;

    @Mock
    private ModRepository modRepository;

    @InjectMocks
    private TagServiceImpl service;

    @Test
    void getAllTags_shouldReturnMappedTags() {
        Tag tag = new Tag();
        tag.setId(1L);
        tag.setName("tech");
        when(tagRepository.findAll()).thenReturn(List.of(tag));

        List<TagDto> result = service.getAllTags();

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getName()).isEqualTo("tech");
    }

    @Test
    void getTagById_shouldReturnTag() {
        Tag tag = new Tag();
        tag.setId(12L);
        tag.setName("tech");
        when(tagRepository.findById(12L)).thenReturn(Optional.of(tag));

        TagDto result = service.getTagById(12L);

        assertThat(result.getId()).isEqualTo(12L);
        assertThat(result.getName()).isEqualTo("tech");
    }

    @Test
    void getTagById_shouldThrowWhenMissing() {
        when(tagRepository.findById(12L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getTagById(12L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Tag not found: 12");
    }

    @Test
    void createTag_shouldSaveTrimmedName() {
        TagCreateDto dto = new TagCreateDto();
        dto.setName("  tech  ");
        Tag saved = new Tag();
        saved.setId(3L);
        saved.setName("tech");

        when(tagRepository.existsByNameIgnoreCase("  tech  ")).thenReturn(false);
        when(tagRepository.save(any(Tag.class))).thenReturn(saved);

        TagDto result = service.createTag(dto);

        assertThat(result.getName()).isEqualTo("tech");
        verify(tagRepository).save(any(Tag.class));
    }

    @Test
    void createTag_shouldRejectBlankName() {
        TagCreateDto dto = new TagCreateDto();
        dto.setName("   ");

        assertThatThrownBy(() -> service.createTag(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Tag name is required");
    }

    @Test
    void createTag_shouldRejectDuplicateName() {
        TagCreateDto dto = new TagCreateDto();
        dto.setName("tech");
        when(tagRepository.existsByNameIgnoreCase("tech")).thenReturn(true);

        assertThatThrownBy(() -> service.createTag(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Tag name already exists: tech");
    }

    @Test
    void updateTag_shouldPersistTrimmedName() {
        TagCreateDto dto = new TagCreateDto();
        dto.setName("  magic  ");
        Tag tag = new Tag();
        tag.setId(5L);
        tag.setName("old");

        when(tagRepository.findById(5L)).thenReturn(Optional.of(tag));
        when(tagRepository.existsByNameIgnoreCaseAndIdNot("  magic  ", 5L)).thenReturn(false);
        when(tagRepository.save(any(Tag.class))).thenReturn(tag);

        TagDto result = service.updateTag(5L, dto);

        assertThat(result.getName()).isEqualTo("magic");
        assertThat(tag.getName()).isEqualTo("magic");
    }

    @Test
    void updateTag_shouldRejectDuplicateName() {
        TagCreateDto dto = new TagCreateDto();
        dto.setName("tech");
        Tag tag = new Tag();
        tag.setId(5L);
        tag.setName("old");

        when(tagRepository.findById(5L)).thenReturn(Optional.of(tag));
        when(tagRepository.existsByNameIgnoreCaseAndIdNot("tech", 5L)).thenReturn(true);

        assertThatThrownBy(() -> service.updateTag(5L, dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Tag name already exists: tech");
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

    @Test
    void syntheticLambdas_shouldBeInvoked() throws Exception {
        Tag tag = new Tag();
        tag.setName("tech");
        Mod mod = new Mod();
        mod.setTags(Set.of(tag));

        invokeDeclared(TagServiceImpl.class, null, "lambda$getTagById$0", 12L);
        invokeDeclared(TagServiceImpl.class, null, "lambda$updateTag$1", 5L);
        invokeDeclared(TagServiceImpl.class, null, "lambda$deleteTag$2", 9L);
        invokeDeclared(TagServiceImpl.class, null, "lambda$deleteTag$3", tag, mod);
    }

    private Object invokeDeclared(Class<?> owner, Object target, String methodName, Object... args) throws Exception {
        for (var candidate : owner.getDeclaredMethods()) {
            if (!candidate.getName().equals(methodName) || candidate.getParameterCount() != args.length) {
                continue;
            }
            boolean matches = true;
            for (int i = 0; i < candidate.getParameterTypes().length; i++) {
                if (args[i] == null) {
                    if (candidate.getParameterTypes()[i].isPrimitive()) {
                        matches = false;
                        break;
                    }
                    continue;
                }
                if (!candidate.getParameterTypes()[i].isAssignableFrom(args[i].getClass())) {
                    matches = false;
                    break;
                }
            }
            if (matches) {
                candidate.setAccessible(true);
                return candidate.invoke(target, args);
            }
        }
        throw new NoSuchMethodException(methodName);
    }
}
