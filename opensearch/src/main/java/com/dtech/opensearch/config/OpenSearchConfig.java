package com.dtech.opensearch.config;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.opensearch.client.RestClient;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.opensearch.client.transport.rest_client.RestClientTransport;

@Configuration
public class OpenSearchConfig {
    @Value("${opensearch.uris:http://localhost:9200}")
    private String opensearchUri;
    @Value("${opensearch.username:admin}")
    private String username;
    @Value("${opensearch.password:Opensearch-node-2025}")
    private String password;
    @Value("${opensearch.connection-timeout-millis:1000}")
    private int connectionTimeoutMillis;
    @Value("${opensearch.socket-timeout-millis:3000}")
    private int socketTimeoutMillis;

    @Bean
    public RestClient getRestClient() {
        HttpHost httpHost;
        try {
            java.net.URI uri = new java.net.URI(opensearchUri);
            httpHost = new HttpHost(uri.getHost(), uri.getPort() != -1 ? uri.getPort() : 9200, uri.getScheme());
        } catch (java.net.URISyntaxException e) {
            throw new RuntimeException("Invalid OpenSearch URI: " + opensearchUri, e);
        }

        final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(AuthScope.ANY,
                new UsernamePasswordCredentials(username, password));

        return RestClient.builder(httpHost)
                // Cấu hình HTTP Client (xác thực)
                .setHttpClientConfigCallback(httpClientBuilder -> {
                    httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
                    // Không còn set timeout ở đây nữa
                    return httpClientBuilder;
                })
                // Cấu hình RequestConfig (timeouts)
                .setRequestConfigCallback(requestConfigBuilder -> {
                    requestConfigBuilder.setConnectTimeout(connectionTimeoutMillis); // Timeout kết nối
                    requestConfigBuilder.setSocketTimeout(socketTimeoutMillis);    // Timeout đọc dữ liệu
                    return requestConfigBuilder;
                })
                .build();
    }

    @Bean
    public OpenSearchClient getOpenSearchClient(RestClient restClient) {
        // Tạo Transport cho OpenSearch Client từ RestClient
        RestClientTransport transport = new RestClientTransport(
                restClient,
                new org.opensearch.client.json.jackson.JacksonJsonpMapper() // Sử dụng Jackson for JSON mapping
        );

        // Tạo và trả về OpenSearchClient
        return new OpenSearchClient(transport);
    }
}
