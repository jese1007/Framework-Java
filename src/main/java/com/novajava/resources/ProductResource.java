package com.novajava.resources;

import com.novajava.model.Product;
import com.novajava.core.AbstractNovaResource;
import com.novajava.core.NovaField;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class ProductResource extends AbstractNovaResource<Product> {

    @Override
    public List<NovaField> fields() {
        return List.of(
            new NovaField("name", "Product Name", NovaField.FieldType.TEXT, true, true),
            new NovaField("price", "Unit Price", NovaField.FieldType.NUMBER, true, true),
            new NovaField("stock", "Stock Quantity", NovaField.FieldType.NUMBER, true, true),
            new NovaField("description", "Full Description", NovaField.FieldType.TEXT_AREA, true, false)
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
