package com.notesapp.repositories;

import com.notesapp.entities.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, String> {
    List<Category> findByUserId(String userId);
    Optional<Category> findByUserIdAndName(String userId, String name);
    Optional<Category> findByUserIdAndCategoryId(String userId, String categoryId);
    boolean existsByUserIdAndName(String userId, String name);
}
