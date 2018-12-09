package de.elite12.musikbot.server.api;

import java.security.Principal;

import javax.ws.rs.core.SecurityContext;

import de.elite12.musikbot.server.model.User;

public class SecurityContextImpl implements SecurityContext {

    private final User u;

    public SecurityContextImpl(User user) {
        u = user;
    }

    @Override
    public String getAuthenticationScheme() {
        return u == null ? null : SecurityContext.BASIC_AUTH;
    }

    @Override
    public Principal getUserPrincipal() {
        return u;
    }

    @Override
    public boolean isSecure() {
        return true;
    }

    @Override
    public boolean isUserInRole(String role) {
        if (u == null) {
            return false;
        }
        if (role.equalsIgnoreCase("admin")) {
            return u.isAdmin();
        }
        return false;
    }

}
