package com.example.minecraftmodscatalog.repository;

import com.example.minecraftmodscatalog.entity.Mod;
import java.util.Optional;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    @Query("select m.id from Mod m join m.author a join m.category c left join m.tags t " +
           "where (:authorName is null or lower(a.name) = :authorName) " +
           "and (:categoryName is null or lower(c.name) = :categoryName) " +
           "and (:tagNames is null or lower(t.name) in :tagNames) " +
           "group by m.id " +
           "having (:tagNames is null or count(distinct t.id) = :tagCount)")
    Page<Long> findModIdsWithFiltersJpql(@Param("authorName") String authorName,
                                         @Param("categoryName") String categoryName,
                                         @Param("tagNames") List<String> tagNames,
                                         @Param("tagCount") long tagCount,
                                         Pageable pageable);

    @Query(value = "select distinct m.id from mods m " +
                   "join authors a on m.author_id = a.id " +
                   "join categories c on m.category_id = c.id " +
                   "left join mod_tags mt on m.id = mt.mod_id " +
                   "left join tags t on mt.tag_id = t.id " +
                   "where (:authorName is null or lower(a.name) = :authorName) " +
                   "and (:categoryName is null or lower(c.name) = :categoryName) " +
                   "and (:tagNames is null or lower(t.name) in :tagNames) " +
                   "group by m.id, a.id, c.id " +
                   "having (:tagNames is null or count(distinct t.id) = :tagCount)",
           countQuery = "select count(*) from (" +
                        "select m.id from mods m " +
                        "join authors a on m.author_id = a.id " +
                        "join categories c on m.category_id = c.id " +
                        "left join mod_tags mt on m.id = mt.mod_id " +
                        "left join tags t on mt.tag_id = t.id " +
                        "where (:authorName is null or lower(a.name) = :authorName) " +
                        "and (:categoryName is null or lower(c.name) = :categoryName) " +
                        "and (:tagNames is null or lower(t.name) in :tagNames) " +
                        "group by m.id " +
                        "having (:tagNames is null or count(distinct t.id) = :tagCount)" +
                        ") filtered_mods",
           nativeQuery = true)
    Page<Long> findModIdsWithFiltersNative(@Param("authorName") String authorName,
                                           @Param("categoryName") String categoryName,
                                           @Param("tagNames") List<String> tagNames,
                                           @Param("tagCount") long tagCount,
                                           Pageable pageable);

    @Query(value = "select m.id from mods m " +
                   "join authors a on m.author_id = a.id " +
                   "join categories c on m.category_id = c.id " +
                   "where (:authorName is null or lower(a.name) = :authorName) " +
                   "and (:categoryName is null or lower(c.name) = :categoryName)",
           countQuery = "select count(*) from mods m " +
                        "join authors a on m.author_id = a.id " +
                        "join categories c on m.category_id = c.id " +
                        "where (:authorName is null or lower(a.name) = :authorName) " +
                        "and (:categoryName is null or lower(c.name) = :categoryName)",
           nativeQuery = true)
    Page<Long> findModIdsWithFiltersNativeWithoutTags(@Param("authorName") String authorName,
                                                      @Param("categoryName") String categoryName,
                                                      Pageable pageable);

    @EntityGraph(attributePaths = {"author", "category", "tags", "versions"})
    @Query("select distinct m from Mod m where m.id in :ids")
    List<Mod> findAllWithGraphByIdIn(@Param("ids") List<Long> ids);

    boolean existsByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);

    boolean existsByAuthorId(Long authorId);

    boolean existsByCategoryId(Long categoryId);

    @Modifying
    @Query(value = "delete from mod_tags where mod_id = :modId", nativeQuery = true)
    void deleteTagLinksByModId(@Param("modId") Long modId);

    @Modifying
    @Query(value = "delete from mod_versions where mod_id = :modId", nativeQuery = true)
    void deleteVersionsByModId(@Param("modId") Long modId);

}
