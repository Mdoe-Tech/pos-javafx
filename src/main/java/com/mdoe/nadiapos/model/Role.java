package com.mdoe.nadiapos.model;

import com.mdoe.nadiapos.exceptions.ValidationException;

import java.security.Permission;
import java.util.HashSet;
import java.util.Set;

public class Role extends BaseEntity {
    private String name;
    private String description;
    private Set<Permission> permissions;

    public Role() {
        super();
        this.permissions = new HashSet<>();
    }

    @Override
    public void validate() throws ValidationException {
        if (name == null || name.trim().isEmpty()) {
            throw new ValidationException("Role name cannot be empty");
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Set<Permission> getPermissions() {
        return permissions;
    }

    public void setPermissions(Set<Permission> permissions) {
        this.permissions = permissions;
    }
}
