package com.bookstore.gui;

/**
 * Interface to allow switching between roles (Buyer ↔ Seller)
 * with proper re-authentication.
 */
public interface RoleSwitcher {
    void switchRole();
}
