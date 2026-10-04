package com.novajava.core;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class NovaField {
    public enum FieldType {
        TEXT, NUMBER, DATE, BOOLEAN, EMAIL, PASSWORD, TEXT_EDITOR
    }

    private String name;
    private String label;
    private FieldType type;
    private boolean searchable;
    private boolean sortable;

    // The ONLY explicit constructor to avoid duplication
    public NovaField(String name, String label, FieldType type, boolean searchable, boolean sortable) {
        this.name = name;
        this.label = label;
        this.type = type;
        this.searchable = searchable;
        this.sortable = sortable;
    }

    // Overloaded constructor for convenience
    public NovaField(String name, String label, FieldType type) {
        this(name, label, type, true, true);
    }
}
