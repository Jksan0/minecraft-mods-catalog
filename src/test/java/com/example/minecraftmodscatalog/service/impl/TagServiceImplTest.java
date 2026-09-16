package com.example.minecraftmodscatalog.service.impl;

import com.example.minecraftmodscatalog.dto.TagCreateDto;
import com.example.minecraftmodscatalog.entity.Mod;
import com.example.minecraftmodscatalog.entity.Tag;
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
