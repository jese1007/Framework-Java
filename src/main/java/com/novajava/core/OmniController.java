package com.novajava.core;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.context.ApplicationContext;

import java.util.List;

@Controller
@RequestMapping("/nova")
public class OmniController {

    @Autowired
    private NovaResourceRegistry registry;

    @Autowired
    private ApplicationContext applicationContext;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("resources", registry.getAllResources());
        return "nova/dashboard";
    }

    @GetMapping("/{resourceName}")
    public String index(@PathVariable String resourceName, Model model) {
        AbstractNovaResource<?> resource = registry.getResource(resourceName);
        if (resource == null) return "error/404";

        JpaRepository repo = findRepository(resource.getEntityClass());
        List<?> data = repo.findAll();

        model.addAttribute("resource", resource);
        model.addAttribute("data", data);
        return "nova/index";
    }

    @GetMapping("/{resourceName}/create")
    public String createForm(@PathVariable String resourceName, Model model) {
        AbstractNovaResource<?> resource = registry.getResource(resourceName);
        if (resource == null) return "error/404";

        try {
            Object entity = resource.getEntityClass().getDeclaredConstructor().newInstance();
            model.addAttribute("entity", entity);
            model.addAttribute("resource", resource);
        } catch (Exception e) {
            return "error/500";
        }
        return "nova/form";
    }

    private JpaRepository findRepository(Class<?> entityClass) {
        String repoName = entityClass.getSimpleName() + "Repository";
        try {
            return (JpaRepository) applicationContext.getBean(repoName);
        } catch (Exception e) {
            throw new RuntimeException("Repository not found for " + entityClass.getSimpleName() + ". Please create a bean named " + repoName);
        }
    }
}
