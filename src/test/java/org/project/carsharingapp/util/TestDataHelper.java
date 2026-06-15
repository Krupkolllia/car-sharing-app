package org.project.carsharingapp.util;

import org.project.carsharingapp.model.Role;
import org.project.carsharingapp.model.User;

public class TestDataHelper {
    public static final String ADD_SCRIPT_PATH = "classpath:database/add-test-data.sql";
    public static final String DELETE_SCRIPT_PATH = "classpath:database/delete-test-data.sql";

    public static User createTestCustomer() {
        return new User()
            .setId(1L)
            .setEmail("test.user1@mail.com")
            .setPassword("$2a$12$DtQUdqgM9yq4yrtbnDpFiO/9NlyzGlhoIBNIgx1Njak.NB7SniGRi")
            .setFirstName("test")
            .setLastName("user1")
            .setRole(Role.CUSTOMER)
            .setDeleted(false);
    }
}
