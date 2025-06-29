package com.ecommerce.product.service;

import com.ecommerce.product.dto.CategoryDto;
import com.ecommerce.product.entity.Category;
import com.ecommerce.product.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public List<CategoryDto> getAllCategories() {
        List<Category> categories = categoryRepository.findAll();
        return categories.stream()
                .map(this::mapToCategoryDto)
                .collect(Collectors.toList());
    }

    public CategoryDto getCategory(Long categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Category not found"));

        return mapToCategoryDto(category);
    }

    public CategoryDto createCategory(String name, String description) {
        if (categoryRepository.findByName(name).isPresent()) {
            throw new RuntimeException("Category with this name already exists");
        }

        Category category = Category.builder()
                .name(name)
                .description(description)
                .build();

        Category savedCategory = categoryRepository.save(category);
        return mapToCategoryDto(savedCategory);
    }

    private CategoryDto mapToCategoryDto(Category category) {
        CategoryDto dto = new CategoryDto();
        dto.setId(category.getId());
        dto.setName(category.getName());
        dto.setDescription(category.getDescription());
        dto.setCreatedAt(category.getCreatedAt());
        return dto;
    }
}