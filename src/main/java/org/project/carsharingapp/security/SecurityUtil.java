package org.project.carsharingapp.security;

import org.project.carsharingapp.model.user.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityUtil {
    public static User getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder
            .getContext()
            .getAuthentication();

        if (authentication != null && authentication.getPrincipal() instanceof User user) {
            return user;
        }

        throw new IllegalStateException(
            "Authenticated user is missing or principal is not of type User. "
                + "This method should only be called within authenticated endpoints."
        );
    }
}
