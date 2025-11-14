package com.notesapp.repositories;

import com.notesapp.entities.Note;
import com.notesapp.entities.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NoteRepository extends JpaRepository<Note, String> {

    List<Note> findByUserId(String userId);

    List<Note> findByUserIdOrderByCreatedAtDesc(String userId);

    @Query("SELECT n FROM Note n JOIN n.tags t WHERE t IN :tags")
    List<Note> findByTags(@Param("tags") List<Tag> tags);

    @Query("SELECT DISTINCT t FROM Tag t")
    List<Tag> findExistingTags();

    @Query("SELECT n FROM Note n WHERE n.userId = :userId AND " +
           "(LOWER(n.title) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(n.body) LIKE LOWER(CONCAT('%', :query, '%')))")
    List<Note> searchByText(@Param("userId") String userId, @Param("query") String query);

    @Query("SELECT n FROM Note n WHERE n.userId = :userId AND " +
           "n.createdAt BETWEEN :start AND :end")
    List<Note> findByDateRange(@Param("userId") String userId,
                                @Param("start") LocalDateTime start,
                                @Param("end") LocalDateTime end);

    List<Note> findByUserIdAndIsPinned(String userId, Boolean isPinned);

    List<Note> findByUserIdAndCategory(String userId, String category);
}
