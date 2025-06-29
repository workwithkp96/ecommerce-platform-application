package com.ecommerce.product.repository;

import com.ecommerce.product.document.ProductDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductDocumentRepository extends ElasticsearchRepository<ProductDocument, Long> {
    Page<ProductDocument> findByNameContainingOrDescriptionContaining(
            String name, String description, Pageable pageable);

    Page<ProductDocument> findByCategoryId(Long categoryId, Pageable pageable);
}