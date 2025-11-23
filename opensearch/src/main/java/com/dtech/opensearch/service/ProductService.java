package com.dtech.opensearch.service;

import com.dtech.opensearch.entity.Product;
import com.dtech.opensearch.repository.ProductRepository;
import com.dtech.opensearch.search.ProductSearchRepository;
import com.github.javafaker.Faker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.Instant;
import java.util.*;

@Service
public class ProductService {
    @Autowired
    private ProductRepository productRepo;
    @Autowired
    private ProductSearchRepository productSearchRepo;

    private static final int BATCH_SIZE = 1000;
    private final Faker faker = new Faker();
    private final Faker fakerVi = new Faker(new Locale("vi", "VN"));

    @Transactional
    public long initData(int amount) throws IOException {
        List<Product> productsToSave = new ArrayList<>();
        long startTime = System.currentTimeMillis();

        for (int i = 1; i <= amount; i++) {
            Product p = new Product();
            String uuid = UUID.randomUUID().toString();
            p.setName(fakerVi.commerce().productName() + " (" + uuid.substring(0, 8) + ")");
            p.setDescription(fakerVi.lorem().sentence());
            p.setPrice(Math.round(faker.number().randomDouble(2, 1, 99999)) / 1.0); // Giá từ 1.00 đến 99.99
            productsToSave.add(p);

            if (productsToSave.size() == BATCH_SIZE || i == amount) {
                List<Product> savedProductsInJpa = productRepo.saveAll(productsToSave);
                productSearchRepo.saveAll(savedProductsInJpa);
                productsToSave.clear();
            }
        }
        long endTime = System.currentTimeMillis();
        return endTime - startTime;
    }

    @Transactional
    public Product save(Product p) {
        Product saved = null;
        try {
            saved = productRepo.save(p);
            productSearchRepo.save(saved);
        } catch (Exception e) {
            throw new RuntimeException("Failed to index to OpenSearch, rolling back DB", e);
        }
        return saved;
    }

    public Page<Product> findAll(Pageable pageable) {
        return productRepo.findAll(pageable);
    }


    public Page<Product> findAllOpenSearch(Pageable pageable) throws IOException {
        return productSearchRepo.findAll(pageable);
    }

    public Product getOrc(Long id) {
        Optional<Product> productOptional = productRepo.findById(id);
        if (productOptional.isPresent()) {
            return productOptional.get();
        } else {
            throw new RuntimeException("Product not found with ID: " + id);
        }
    }


    public Product getOpenSrc(Long id) throws IOException {
        return productSearchRepo.get(id);
    }

    public void delete(Long id) throws IOException {
        productRepo.deleteById(id);
        productSearchRepo.deleteById(id);
    }
}