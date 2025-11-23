package com.dtech.opensearch.search;


import com.dtech.opensearch.entity.Product;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch._types.SortOptions;
import org.opensearch.client.opensearch._types.SortOptionsBuilders;
import org.opensearch.client.opensearch._types.SortOrder;
import org.opensearch.client.opensearch.core.BulkRequest;
import org.opensearch.client.opensearch.core.SearchRequest;
import org.opensearch.client.opensearch.core.SearchResponse;
import org.opensearch.client.opensearch.core.search.Hit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Repository
public class ProductSearchRepository {

    private final OpenSearchClient openSearchClient;

    // Sử dụng Constructor Injection để Spring tự động tiêm OpenSearchClient vào
    public ProductSearchRepository(OpenSearchClient openSearchClient) {
        this.openSearchClient = openSearchClient;
    }

    /**
     * Lưu một sản phẩm vào OpenSearch index.
     * ID của sản phẩm phải được thiết lập trước khi gọi phương thức này.
     *
     * @param product Sản phẩm cần lưu.
     * @return Sản phẩm đã được lưu.
     * @throws IOException Nếu có lỗi trong quá trình giao tiếp với OpenSearch.
     */
    public Product save(Product product) throws IOException {
        // Kiểm tra ID để đảm bảo sản phẩm có ID trước khi index
        if (product.getId() == null) {
            throw new IllegalArgumentException("Product ID must not be null for OpenSearch indexing.");
        }

        // Thực hiện lệnh index (thêm/cập nhật) vào OpenSearch
        openSearchClient.index(i -> i
                .index("products") // Tên index trong OpenSearch, bạn có thể cấu hình động
                .id(product.getId().toString()) // OpenSearch ID thường là String
                .document(product) // Đối tượng Product sẽ được chuyển đổi thành JSON
        );
        return product;
    }

    public List<Product> saveAll(List<Product> products) throws IOException {
        BulkRequest.Builder br = new BulkRequest.Builder();

        for (Product product : products) {
            if (product.getId() == null) {
                throw new IllegalArgumentException("Product ID must not be null for OpenSearch indexing in batch.");
            }
            br.operations(op -> op
                    .index(idx -> idx
                            .index("products")
                            .id(product.getId().toString())
                            .document(product)
                    )
            );
        }

        // Thực hiện bulk request
        openSearchClient.bulk(br.build());

        return products;
    }


    /**
     * Tìm tất cả các sản phẩm trong OpenSearch index.
     * Lưu ý: Đối với tập dữ liệu lớn, hãy cân nhắc sử dụng phân trang.
     *
     * @return Danh sách tất cả sản phẩm.
     * @throws IOException Nếu có lỗi trong quá trình giao tiếp với OpenSearch.
     */
    public List<Product> findAll() throws IOException {
        SearchResponse<Product> searchResponse = openSearchClient.search(s -> s
                        .index("products")
                        .query(q -> q.matchAll(m -> {
                            return null;
                        }))
                , Product.class);

        // Trích xuất các tài liệu từ kết quả tìm kiếm
        return searchResponse.hits().hits().stream()
                .map(Hit::source)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }


    /**
     * Tìm tất cả các sản phẩm trong OpenSearch index với phân trang.
     *
     * @param pageable Đối tượng Pageable chứa thông tin phân trang (page, size, sort).
     * @return Một đối tượng Page chứa danh sách sản phẩm và thông tin phân trang.
     * @throws IOException Nếu có lỗi trong quá trình giao tiếp với OpenSearch.
     */
    public Page<Product> findAll(Pageable pageable) throws IOException {
        SearchRequest.Builder searchRequestBuilder = new SearchRequest.Builder()
            .index("products")
            .from(pageable.getPageNumber() * pageable.getPageSize())
            .size(pageable.getPageSize())
            .query(q -> q.matchAll(m -> {
                return null;
            }));

        if (pageable.getSort().isSorted()) {
            for (Sort.Order order : pageable.getSort()) {
                SortOptions sortOption = SortOptions.of(s -> s
                        .field(f -> f
                                .field(order.getProperty())
                                .order(order.isAscending() ? SortOrder.Asc : SortOrder.Desc)
                        )
                );
                searchRequestBuilder.sort(sortOption);
            }
        }

        SearchResponse<Product> searchResponse = openSearchClient.search(
                searchRequestBuilder.build(),
                Product.class
        );

        List<Product> products = searchResponse.hits().hits().stream()
                .map(Hit::source)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        long totalHits = searchResponse.hits().total() != null ? searchResponse.hits().total().value() : 0;

        return new PageImpl<>(products, pageable, totalHits);
    }

    /**
     * Lấy một sản phẩm theo ID từ OpenSearch index.
     *
     * @param id ID của sản phẩm.
     * @return Sản phẩm tìm được, hoặc null nếu không tìm thấy.
     * @throws IOException Nếu có lỗi trong quá trình giao tiếp với OpenSearch.
     */
    public Product get(Long id) throws IOException {
        return openSearchClient.get(g -> g
                        .index("products")
                        .id(id.toString())
                , Product.class).source();
    }

    /**
     * Xóa một sản phẩm theo ID khỏi OpenSearch index.
     *
     * @param id ID của sản phẩm cần xóa.
     * @throws IOException Nếu có lỗi trong quá trình giao tiếp với OpenSearch.
     */
    public void deleteById(Long id) throws IOException {
        openSearchClient.delete(d -> d
                .index("products")
                .id(id.toString())
        );
    }

    // Bạn có thể thêm các phương thức tìm kiếm phức tạp hơn ở đây,
    // ví dụ: tìm kiếm theo tên, mô tả, khoảng giá, v.v.
    /*
    public List<Product> searchByName(String name) throws IOException {
        SearchResponse<Product> searchResponse = openSearchClient.search(s -> s
            .index("products")
            .query(q -> q
                .match(m -> m
                    .field("name")
                    .query(name)
                )
            ),
            Product.class
        );
        return searchResponse.hits().hits().stream()
                .map(Hit::source)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
    */
}