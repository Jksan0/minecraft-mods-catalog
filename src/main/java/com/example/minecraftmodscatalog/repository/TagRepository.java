package com.example.minecraftmodscatalog.repository;

import com.example.minecraftmodscatalog.entity.Tag;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TagRepository extends JpaRepository<Tag, Long> {
    Optional<Tag> findByNameIgnoreCase(String name);

    @Query("select t from Tag t where lower(t.name) in :names")
    List<Tag> findAllByLowerNameIn(@Param("names") Collection<String> names);

    boolean existsByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);
}
