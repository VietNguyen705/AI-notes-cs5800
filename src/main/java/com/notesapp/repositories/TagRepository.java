package com.notesapp.repositories;

import com.notesapp.entities.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TagRepository extends JpaRepository<Tag, String> {

    Optional<Tag> findByName(String name);

    Optional<Tag> findByNameIgnoreCase(String name);

    boolean existsByName(String name);
}
