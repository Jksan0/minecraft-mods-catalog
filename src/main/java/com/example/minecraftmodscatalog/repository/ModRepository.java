package com.example.minecraftmodscatalog.repository;

import com.example.minecraftmodscatalog.entity.Mod;
import java.util.Optional;
import java.util.List;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Query;

public interface ModRepository extends JpaRepository<Mod, Long> {
    @Query("select m from Mod m where lower(m.author.name) = lower(:authorName)")
    List<Mod> findByAuthorName(@Param("authorName") String authorName);

    @Query("select m from Mod m")
    List<Mod> findAllNaiveForNPlusOne();

    @EntityGraph(attributePaths = {"author", "category", "tags", "versions"})
    @Query("select distinct m from Mod m")
    List<Mod> findAllWithGraph();
    
    @EntityGraph(attributePaths = {"author", "category", "tags", "versions"})
    @Query("select m from Mod m where m.id = :id")
    Optional<Mod> findByIdWithGraph(@Param("id") Long id);

    boolean existsByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);

    boolean existsByAuthorId(Long authorId);

    @Modifying
    @Query(value = "delete from mod_tags where mod_id = :modId", nativeQuery = true)
    void deleteTagLinksByModId(@Param("modId") Long modId);

    @Modifying
    @Query(value = "delete from mod_versions where mod_id = :modId", nativeQuery = true)
    void deleteVersionsByModId(@Param("modId") Long modId);

}
