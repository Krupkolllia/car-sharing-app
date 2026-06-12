package org.project.carsharingapp.model;

import org.springframework.security.core.GrantedAuthority;

public enum Role implements GrantedAuthority {
    CUSTOMER,
    MANAGER;

    @Override
    public String getAuthority() {
        return "ROLE_" + name();
    }
}
