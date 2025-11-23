package com.dtech.opensearch.controller;

import com.dtech.opensearch.dto.ApiResponse;
import com.dtech.opensearch.entity.Product;
import com.dtech.opensearch.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/products")
public class ProductController {
    @Autowired
    private ProductService service;

    @PostMapping("/init/{amount}")
    public ResponseEntity<String> initData(@PathVariable int amount) throws IOException {
        long duration = service.initData(amount);
        return ResponseEntity.ok("Initialized " + amount + " products ! in " + duration + " ms");
    }


    @PostMapping
    public ResponseEntity<Product> create(@RequestBody Product p) {
        return ResponseEntity.ok(service.save(p));
    }

    @GetMapping("orc/list")
    public ResponseEntity<ApiResponse<Page<Product>>> list(@PageableDefault(size = 10, page = 0) Pageable pageable) throws IOException {
        long startTime = System.currentTimeMillis();
        Page<Product> productsPage = service.findAll(pageable);
        return ResponseEntity.ok(ApiResponse.success("Get in [" + (System.currentTimeMillis() - startTime) + "] ms", productsPage));
    }

    @GetMapping("orc/{id}")
    public ResponseEntity<ApiResponse<Product>> getOrc(@PathVariable Long id) throws IOException {
        long startTime = System.currentTimeMillis();
        Product product = service.getOrc(id);
        return ResponseEntity.ok(ApiResponse.success("Get in [" + (System.currentTimeMillis() - startTime) + "] ms", product));
    }


    @GetMapping("open-src/{id}")
    public ResponseEntity<ApiResponse<Product>> getOpenSrc(@PathVariable Long id) throws IOException {
        long startTime = System.currentTimeMillis();
        Product product =  service.getOpenSrc(id);
        return ResponseEntity.ok(ApiResponse.success("Get in [" + (System.currentTimeMillis() - startTime) + "] ms", product));
    }

    @GetMapping("open-src/list")
    public ResponseEntity<ApiResponse<Page<Product>>> listOpenSrc(@PageableDefault(size = 10, page = 0) Pageable pageable) throws IOException {
        long startTime = System.currentTimeMillis();
        Page<Product> productsPage = service.findAllOpenSearch(pageable);
        return ResponseEntity.ok(ApiResponse.success("Get in [" + (System.currentTimeMillis() - startTime) + "] ms", productsPage));
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) throws IOException {
        service.delete(id);
    }
}
