package com.novajava.core;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.context.ApplicationContext;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.persistence.criteria.*;
import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/nova")
public class OmniController {

    @Autowired
    private NovaResourceRegistry registry;

    @Autowired
    private ApplicationContext applicationContext;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        Collection<AbstractNovaResource<?>> allResources = registry.getAllResources();
        List<AbstractNovaResource<?>> resources = new ArrayList<>(allResources);
        model.addAttribute("resources", resources);

        // Map to hold data for all resources: { "resourceName": [records] }
        Map<String, List<?>> allData = new HashMap<>();
        // Map to hold total counts for stats: { "resourceName": count }
        Map<String, Long> resourceCounts = new HashMap<>();

        for (AbstractNovaResource<?> res : resources) {
            try {
                String name = res.getEntityClass().getSimpleName().toLowerCase();
                JpaRepository repo = findRepository(res.getEntityClass());
                List<?> records = repo.findAll();

                allData.put(name, records);
                resourceCounts.put(name, (long) records.size());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        model.addAttribute("allResourceData", allData);
        model.addAttribute("resourceCounts", resourceCounts);

        // Specifically extract Product data for the graphs (as we did before)
        AbstractNovaResource<?> productResource = resources.stream()
                .filter(r -> r.getEntityClass().getSimpleName().equalsIgnoreCase("Product"))
                .findFirst()
                .orElse(null);

        if (productResource != null) {
            try {
                List<?> products = allData.get(productResource.getEntityClass().getSimpleName().toLowerCase());
                if (products != null) {
                    List<String> names = new ArrayList<>();
                    List<Double> stocks = new ArrayList<>();
                    List<Double> prices = new ArrayList<>();

                    for (Object p : products) {
                        names.add(getStringValue(p, "name"));
                        stocks.add(getDoubleValue(p, "stock"));
                        prices.add(getDoubleValue(p, "price"));
                    }

                    model.addAttribute("productNames", names);
                    model.addAttribute("productStocks", stocks);
                    model.addAttribute("productPrices", prices);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return "nova/dashboard";
    }

    @GetMapping("/{resourceName}")
    public String index(
            @PathVariable String resourceName,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "") String search,
            @RequestParam(defaultValue = "") String sort,
            Model model) {

        AbstractNovaResource<?> resource = registry.getResource(resourceName);
        if (resource == null) return "error/404";

        JpaRepository repo = findRepository(resource.getEntityClass());

        Sort sortObject = Sort.unsorted();
        if (sort != null && !sort.isEmpty()) {
            String[] sortParts = sort.split(",");
            String property = sortParts[0];
            Sort.Direction direction = Sort.Direction.ASC;
            if (sortParts.length > 1 && sortParts[1].equalsIgnoreCase("desc")) {
                direction = Sort.Direction.DESC;
            }
            sortObject = Sort.by(direction, property);
        }

        Page pageResult;
        if (repo instanceof JpaSpecificationExecutor) {
            JpaSpecificationExecutor executor = (JpaSpecificationExecutor) repo;

            Specification spec = (root, query, cb) -> {
                if (search == null || search.trim().isEmpty()) return null;

                String searchTrimmed = search.trim();
                String searchLower = searchTrimmed.toLowerCase();
                List<Predicate> predicates = new ArrayList<>();

                boolean isNumericSearch = isNumber(searchTrimmed);

                for (NovaField field : resource.fields()) {
                    if (!field.isSearchable()) continue;

                    Path path = root.get(field.getName());

                    if (!isNumericSearch && isStringField(field)) {
                        predicates.add(cb.like(cb.lower(path), "%" + searchLower + "%"));
                    } else if (isNumericSearch && field.getType() == NovaField.FieldType.NUMBER) {
                        try {
                            if (path.getJavaType() == Double.class || path.getJavaType() == double.class) {
                                predicates.add(cb.equal(path, Double.parseDouble(searchTrimmed)));
                            } else if (path.getJavaType() == Integer.class || path.getJavaType() == int.class) {
                                predicates.add(cb.equal(path, Integer.parseInt(searchTrimmed)));
                            } else if (path.getJavaType() == Long.class || path.getJavaType() == long.class) {
                                predicates.add(cb.equal(path, Long.parseLong(searchTrimmed)));
                            }
                        } catch (Exception e) { }
                    }
                }
                return predicates.isEmpty() ? null : cb.or(predicates.toArray(new Predicate[0]));
            };
            pageResult = executor.findAll(spec, PageRequest.of(page, 10, sortObject));
        } else {
            pageResult = repo.findAll(PageRequest.of(page, 10, sortObject));
        }

        model.addAttribute("resource", resource);
        model.addAttribute("data", pageResult.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", pageResult.getTotalPages());
        model.addAttribute("totalItems", pageResult.getTotalElements());
        model.addAttribute("search", search);
        model.addAttribute("sort", sort);

        return "nova/index";
    }

    @GetMapping("/{resourceName}/view/{id}")
    public String view(@PathVariable String resourceName, @PathVariable Long id, Model model) throws Throwable {
        AbstractNovaResource<?> resource = registry.getResource(resourceName);
        if (resource == null) return "error/404";
        JpaRepository repo = findRepository(resource.getEntityClass());
        Object entity = repo.findById(id).orElseThrow(() -> new RuntimeException("Not found"));
        model.addAttribute("entity", entity);
        model.addAttribute("resource", resource);
        return "nova/view";
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

    @GetMapping("/{resourceName}/edit/{id}")
    public String editForm(@PathVariable String resourceName, @PathVariable Long id, Model model) throws Throwable {
        AbstractNovaResource<?> resource = registry.getResource(resourceName);
        if (resource == null) return "error/404";
        JpaRepository repo = findRepository(resource.getEntityClass());
        Object entity = repo.findById(id).orElseThrow(() -> new RuntimeException("Not found"));
        model.addAttribute("entity", entity);
        model.addAttribute("resource", resource);
        return "nova/form";
    }

    @PostMapping("/{resourceName}/save")
    public String save(@PathVariable String resourceName, @RequestParam Map<String, String> allParams,
  RedirectAttributes ra) throws Throwable {
        AbstractNovaResource<?> resource = registry.getResource(resourceName);
        if (resource == null) return "error/404";
        try {
            JpaRepository repo = findRepository(resource.getEntityClass());
            Object entity;
            String idStr = allParams.get("id");
            if (idStr != null && !idStr.isEmpty()) {
                Long id = Long.parseLong(idStr);
                entity = repo.findById(id).orElseThrow(() -> new RuntimeException("Not found"));
            } else {
                entity = resource.getEntityClass().getDeclaredConstructor().newInstance();
            }

            for (NovaField field : resource.fields()) {
                String value = allParams.get(field.getName());
                if (value != null) {
                    if (validateType(field, value)) {
                        setFieldValue(entity, field, value);
                    } else {
                        ra.addFlashAttribute("error", "Invalid value for " + field.getLabel());
                    }
                }
            }

            repo.save(entity);
            ra.addFlashAttribute("message", "Saved successfully!");
        } catch (Exception e) {
            e.printStackTrace();
            return "error/500";
        }
        return "redirect:/nova/" + resourceName;
    }

    @GetMapping("/{resourceName}/delete/{id}")
    public String delete(@PathVariable String resourceName, @PathVariable Long id, RedirectAttributes ra) {
        AbstractNovaResource<?> resource = registry.getResource(resourceName);
        JpaRepository repo = findRepository(resource.getEntityClass());
        repo.deleteById(id);
        ra.addFlashAttribute("message", "Deleted successfully!");
        return "redirect:/nova/" + resourceName;
    }

    private void setFieldValue(Object entity, NovaField field, String value) throws Exception {
        Field f = entity.getClass().getDeclaredField(field.getName());
        f.setAccessible(true);
        if (field.getType() == NovaField.FieldType.NUMBER) {
            if (f.getType() == Double.class || f.getType() == double.class) {
                f.set(entity, Double.parseDouble(value));
            } else if (f.getType() == Integer.class || f.getType() == int.class) {
                f.set(entity, Integer.parseInt(value));
            } else if (f.getType() == Long.class || f.getType() == long.class) {
                f.set(entity, Long.parseLong(value));
            }
        } else if (field.getType() == NovaField.FieldType.BOOLEAN) {
            f.set(entity, Boolean.parseBoolean(value));
        } else {
            f.set(entity, value);
        }
    }

    private boolean validateType(NovaField field, String value) {
        if (value == null || value.isEmpty()) return true;
        try {
            if (field.getType() == NovaField.FieldType.NUMBER) {
                Double.parseDouble(value);
            } else if (field.getType() == NovaField.FieldType.BOOLEAN) {
                if (!value.equalsIgnoreCase("true") && !value.equalsIgnoreCase("false")) return false;
            }
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private JpaRepository findRepository(Class<?> entityClass) {
        String className = entityClass.getSimpleName();
        String camelCaseName = className.substring(0, 1).toLowerCase() + className.substring(1) + "Repository";
        String pascalCaseName = className + "Repository";
        try {
            return (JpaRepository) applicationContext.getBean(camelCaseName);
        } catch (Exception e1) {
            try {
                return (JpaRepository) applicationContext.getBean(pascalCaseName);
            } catch (Exception e2) {
                throw new RuntimeException("Could not find repository bean for " + className);
            }
        }
    }

    private boolean isNumber(String str) {
        try { Double.parseDouble(str); return true; } catch (Exception e) { return false; }
    }

    private boolean isStringField(NovaField field) {
        return field.getType() == NovaField.FieldType.TEXT || field.getType() == NovaField.FieldType.EMAIL ||
        field.getType() == NovaField.FieldType.PASSWORD || field.getType() == NovaField.FieldType.TEXT_AREA ||
        field.getType() == NovaField.FieldType.TEXT_EDITOR;
    }

    private String getStringValue(Object obj, String fieldName) {
        try {
            Field f = obj.getClass().getDeclaredField(fieldName);
            f.setAccessible(true);
            return String.valueOf(f.get(obj));
        } catch (Exception e) { return "Unknown"; }
    }

    private Double getDoubleValue(Object obj, String fieldName) {
        try {
            Field f = obj.getClass().getDeclaredField(fieldName);
            f.setAccessible(true);
            Object val = f.get(obj);
            if (val instanceof Number) return ((Number) val).doubleValue();
            return 0.0;
        } catch (Exception e) { return 0.0; }
    }
}
