package com.novajava.core;

import java.util.List;
import java.util.ArrayList;

public abstract class AbstractNovaResource<T> {
    
    /**
     * Returns the list of fields to be displayed and managed in the admin panel.
     * This is the "Blueprint" of the resource.
     */
    public abstract List<NovaField> fields();

    /**
     * Returns the entity class associated with this resource.
     */
    public abstract Class<T> getEntityClass();

    /**
     * Returns a human-readable label for the resource (e.g., "Users" instead of "UserResource").
     */
    public String getLabel() {
        return getEntityClass().getSimpleName() + "s";
    }
}
