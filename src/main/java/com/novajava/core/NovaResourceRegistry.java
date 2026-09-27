package com.novajava.core;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class NovaResourceRegistry {

    @Autowired
    private ApplicationContext applicationContext;

    private final Map<String, AbstractNovaResource<?>> resources = new HashMap<>();

    @PostConstruct
    public void init() {
        // Find all beans that extend AbstractNovaResource
        Map<String, AbstractNovaResource> beans = applicationContext.getBeansOfType(AbstractNovaResource.class);
        
        beans.forEach((beanName, resource) -> {
            String key = resource.getEntityClass().getSimpleName().toLowerCase();
            resources.put(key, resource);
        });
        
        System.out.println("Nova Framework initialized with " + resources.size() + " resources.");
    }

    public AbstractNovaResource<?> getResource(String name) {
        return resources.get(name.toLowerCase());
    }

    public Collection<AbstractNovaResource<?>> getAllResources() {
        return resources.values();
    }
}
