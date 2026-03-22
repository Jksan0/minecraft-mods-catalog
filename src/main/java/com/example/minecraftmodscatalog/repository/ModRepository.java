package com.example.minecraftmodscatalog.repository;

import com.example.minecraftmodscatalog.entity.Mod;
import java.util.Optional;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Query;

public interface ModRepository extends JpaRepository<Mod, Long> {
    @Query("select m from Mod m where lower(m.author.name) = lower(:authorName)")
    List<Mod> findByAuthorName(@Param("authorName") String authorName);

    @Query("select m from Mod m")
    List<Mod> findAllNaiveForNPlusOne();

    @EntityGraph(attributePaths = {"author", "categories", "tags", "versions"})
    @Query("select distinct m from Mod m")
    List<Mod> findAllWithGraph();

    @Query("""
            select distinct m from Mod m
            left join fetch m.author
            left join fetch m.categories
            left join fetch m.tags
            left join fetch m.versions
            """)
    List<Mod> findAllWithFetchJoin();

    @EntityGraph(attributePaths = {"author", "categories", "tags", "versions"})
    @Query("select m from Mod m where m.id = :id")
    Optional<Mod> findByIdWithGraph(@Param("id") Long id);
}
