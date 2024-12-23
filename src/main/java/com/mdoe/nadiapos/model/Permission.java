package com.mdoe.nadiapos.model;

import com.mdoe.nadiapos.exceptions.ValidationException;

public class Permission extends BaseEntity {
    private String name;
    private String description;
    private String module;

    @Override
    public void validate() throws ValidationException {
        if (name == null || name.trim().isEmpty()) {
            throw new ValidationException("Permission name cannot be empty");
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

    public String getModule() {
        return module;
    }

    public void setModule(String module) {
        this.module = module;
    }
}
