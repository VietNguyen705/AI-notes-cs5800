package com.notesapp.controllers;

import com.notesapp.entities.Category;
import com.notesapp.repositories.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/categories")
@CrossOrigin(origins = "*")
public class CategoryController {

    @Autowired
    private CategoryRepository categoryRepository;

    @GetMapping
    public ResponseEntity<List<Category>> getUserCategories(@RequestParam String userId) {
        List<Category> categories = categoryRepository.findByUserId(userId);
        return ResponseEntity.ok(categories);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Category> getCategoryById(@PathVariable String id) {
        return categoryRepository.findById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Category> createCategory(@RequestBody Map<String, Object> categoryData) {
        try {
            String userId = (String) categoryData.get("userId");
            String name = (String) categoryData.get("name");

            if (name == null || name.isBlank()) {
                return ResponseEntity.badRequest().build();
            }

            if (categoryRepository.existsByUserIdAndName(userId, name)) {
                return ResponseEntity.status(HttpStatus.CONFLICT).build();
            }

            Category category = new Category();
            category.setUserId(userId);
            category.setName(name);
            category.setDescription((String) categoryData.getOrDefault("description", ""));
            category.setColor((String) categoryData.getOrDefault("color", "#667eea"));

            Category saved = categoryRepository.save(category);
            return ResponseEntity.status(HttpStatus.CREATED).body(saved);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Category> updateCategory(@PathVariable String id, @RequestBody Map<String, Object> updates) {
        return categoryRepository.findById(id)
            .map(category -> {
                if (updates.containsKey("name")) {
                    category.setName((String) updates.get("name"));
                }
                if (updates.containsKey("description")) {
                    category.setDescription((String) updates.get("description"));
                }
                if (updates.containsKey("color")) {
                    category.setColor((String) updates.get("color"));
                }

                Category updated = categoryRepository.save(category);
                return ResponseEntity.ok(updated);
            })
            .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable String id) {
        return categoryRepository.findById(id)
            .map(category -> {
                categoryRepository.delete(category);
                return ResponseEntity.ok().<Void>build();
            })
            .orElse(ResponseEntity.notFound().build());
    }
}
