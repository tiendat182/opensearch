package com.dtech.opensearch;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories(basePackages = "com.dtech.opensearch.repository")
public class OpensearchApplication {

	public static void main(String[] args) {
		SpringApplication.run(OpensearchApplication.class, args);
	}

}
