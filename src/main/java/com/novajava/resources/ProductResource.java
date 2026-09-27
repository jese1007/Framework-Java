package com.novajava.resources;

import com.novajava.core.AbstractNovaResource;
import com.novajava.core.NovaField;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class ProductResource extends AbstractNovaResource<Product> {

    @Override
    public List<NovaField> fields() {
        // This defines exactly what appears in the Nova Admin UI
        return List.of(
            new NovaField("name", "Product Name", NovaField.FieldType.TEXT),
            new NovaField("price", "Unit Price", NovaField.FieldType.NUMBER),
            new NovaField("stock", "Stock Quantity", NovaField.FieldType.NUMBER),
            new NovaField("description", "Full Description", NovaField.FieldType.TEXT)
        );
    }

    @Override
    public Class<Product> getEntityClass() {
        return Product.class;
    }

    @Override
    public String getLabel() {
        return "Inventory Products";
    }
}
