package com.novajava.core;

import com.novajava.model.Product;
import com.novajava.repository.ProductRepository;
import com.novajava.model.User;
import com.novajava.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import java.util.*;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        // Initialize Products
        if (productRepository.count() == 0) {
            productRepository.saveAll(List.of(
                new Product(null, "MacBook Pro", 2499.0, "Apple M3 Max, 32GB RAM", 10),
                new Product(null, "Dell XPS 15", 1899.0, "Intel i9, OLED Display", 5),
                new Product(null, "Logitech MX Master", 99.0, "Ergonomic Wireless Mouse", 50)
            ));
            System.out.println("Sample data initialized: Products added to database.");
        }

        // Initialize Admin User
        if (userRepository.count() == 0) {
            User admin = User.builder()
                    .username("admin")
                    .password(passwordEncoder.encode("admin123"))
                    .email("admin@nova.com")
                    .fullName("Nova Administrator")
                    .roles(Set.of("ROLE_ADMIN"))
                    .build();
            userRepository.save(admin);
            System.out.println("Default admin user created: admin / admin123");
        }
    }
}
