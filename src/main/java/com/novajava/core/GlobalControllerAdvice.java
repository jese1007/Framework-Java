package com.novajava.core;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import java.util.Collection;

@ControllerAdvice
public class GlobalControllerAdvice {

    @Autowired
    private NovaResourceRegistry registry;

    @ModelAttribute("resources")
    public Collection<AbstractNovaResource<?>> addResourcesToModel() {
        return registry.getAllResources();
    }
}
