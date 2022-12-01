package com.modsensoftware.marketplace.enums;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;

/**
 * @author andrey.demyanchik on 10/31/2022
 */
@RequiredArgsConstructor
public enum Role implements GrantedAuthority {
    MANAGER("MANAGER"),
    DIRECTOR("DIRECTOR"),
    STORAGE_MANAGER("STORAGE_MANAGER");

    private final String value;

    @Override
    public String getAuthority() {
        return value;
    }
}
