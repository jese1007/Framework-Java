package com.novajava.core;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NovaField {
    public enum FieldType {
        TEXT, NUMBER, DATE, BOOLEAN, EMAIL, PASSWORD
    }

    private String name;
    private String label;
    private FieldType type;
    private boolean searchable;
    private boolean sortable;

    public NovaField(String name, String label, FieldType type) {
        this.name = name;
        this.label = label;
        this.type = type;
        this.searchable = true;
        this.sortable = true;
    }
}
