package com.novajava.core;

import com.novajava.resources.Product;
import com.novajava.resources.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private ProductRepository productRepository;

    @Override
    public void run(String... args) {
        if (productRepository.count() == 0) {
            productRepository.saveAll(List.of(
                new Product(null, "MacBook Pro", 2499.0, "Apple M3 Max, 32GB RAM", 10),
                new Product(null, "Dell XPS 15", 1899.0, "Intel i9, OLED Display", 5),
                new Product(null, "Logitech MX Master", 99.0, "Ergonomic Wireless Mouse", 50)
            ));
            System.out.println("Sample data initialized: Products added to database.");
        }
    }
}
