package com.novajava.resources;

import com.novajava.model.User;
import com.novajava.core.AbstractNovaResource;
import com.novajava.core.NovaField;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class UserResource extends AbstractNovaResource<User> {

    @Override
    public Class<User> getEntityClass() {
        return User.class;
    }

    @Override
    public List<NovaField> fields() {
        return Arrays.asList(
            new NovaField("username", "Username", NovaField.FieldType.TEXT),
            new NovaField("password", "Password", NovaField.FieldType.PASSWORD,false,false),
            new NovaField("email", "Email", NovaField.FieldType.EMAIL),
            new NovaField("fullName", "Full Name", NovaField.FieldType.TEXT)
        );
    }
}
